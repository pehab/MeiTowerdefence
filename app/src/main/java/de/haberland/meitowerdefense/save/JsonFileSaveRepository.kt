package de.haberland.meitowerdefense.save

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** File storage without Android dependencies, so recovery and failed writes can be JVM-tested. */
class JsonFileSaveRepository(private val directory: File) : SaveRepository {
    private val file = File(directory, "savegame.json")
    private val backup = File(directory, "savegame.json.bak")
    private val json = Json { ignoreUnknownKeys = true }

    @Synchronized
    override fun load(): SaveData = readOrArchive(file) ?: readOrArchive(backup) ?: SaveData()

    @Synchronized
    override fun save(data: SaveData) {
        val encoded = json.encodeToString(data)
        val previous = readOrArchive(file)
        // Preserve the previous valid generation. A corrupt primary must never replace
        // a usable backup. On the very first save, seed both copies with the new state.
        if (previous != null) {
            replaceAtomically(backup, json.encodeToString(previous))
        } else if (!backup.exists()) {
            replaceAtomically(backup, encoded)
        }
        replaceAtomically(file, encoded)
    }

    @Synchronized
    override fun reset(data: SaveData) {
        val encoded = json.encodeToString(data)
        // Replace recovery first: after success no damaged primary can resurrect old progress.
        replaceAtomically(backup, encoded)
        replaceAtomically(file, encoded)
    }

    private fun readOrArchive(source: File): SaveData? {
        if (!source.exists()) return null
        val text = source.readText() // I/O failures propagate; they are not evidence of corrupt JSON.
        return try {
            json.decodeFromString<SaveData>(text)
        } catch (_: SerializationException) {
            archive(source)
            null
        } catch (_: IllegalArgumentException) {
            archive(source)
            null
        }
    }

    private fun archive(source: File) {
        val archived = File.createTempFile("savegame-corrupt-", ".json", directory)
        try {
            Files.move(source.toPath(), archived.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } catch (error: IOException) {
            archived.delete()
            throw error // Do not allow a fresh save to overwrite unpreserved evidence.
        }
    }

    private fun replaceAtomically(destination: File, content: String) {
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IOException("Cannot create save directory")
        }
        val temporary = File.createTempFile("savegame-write-", ".tmp", directory)
        try {
            FileOutputStream(temporary).use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
                stream.fd.sync()
            }
            try {
                Files.move(temporary.toPath(), destination.toPath(),
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                // Same-directory rename on filesystems that do not expose ATOMIC_MOVE.
                Files.move(temporary.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            temporary.delete()
        }
    }
}
