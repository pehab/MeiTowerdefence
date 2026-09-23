package de.haberland.meitowerdefense.save

/** Load/save the whole save-game blob. [FileSaveRepository] is the real, file-backed implementation. */
interface SaveRepository {
    fun load(): SaveData
    fun save(data: SaveData)
}
