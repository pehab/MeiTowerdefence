package de.haberland.meitowerdefense.save

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Reads/writes the save-game state as one small JSON file in app-private storage. No
 * Room: the data is flat (a handful of upgrade levels, a small fixed list of level
 * records) with no relations or queries beyond "load everything, save everything" - a
 * database would add ceremony (entities, DAOs, migrations) without buying anything a
 * plain file doesn't already give for this shape of data.
 *
 * A corrupted or unreadable file falls back to a fresh [SaveData] rather than crashing -
 * losing progress is bad, but crashing on every launch afterwards would be worse.
 */
class FileSaveRepository(context: Context) : SaveRepository {
    private val file = File(context.filesDir, "savegame.json")
    private val json = Json { ignoreUnknownKeys = true }

    override fun load(): SaveData {
        if (!file.exists()) return SaveData()
        return runCatching { json.decodeFromString<SaveData>(file.readText()) }.getOrDefault(SaveData())
    }

    override fun save(data: SaveData) {
        file.writeText(json.encodeToString(data))
    }
}
