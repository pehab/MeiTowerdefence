package de.haberland.meitowerdefense.save

/** Load/save the whole save-game blob. [FileSaveRepository] is the real, file-backed implementation. */
interface SaveRepository {
    fun load(): SaveData
    fun save(data: SaveData)
    /** Explicit fresh start; file-backed stores must replace their recovery copy as well. */
    fun reset(data: SaveData) = save(data)
}
