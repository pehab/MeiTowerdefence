package de.haberland.meitowerdefense.save

/** In-memory stand-in for [FileSaveRepository], used only in tests - no real file I/O. */
class FakeSaveRepository(initial: SaveData = SaveData()) : SaveRepository {
    private var current: SaveData = initial
    var saveCallCount = 0
        private set

    override fun load(): SaveData = current

    override fun save(data: SaveData) {
        current = data
        saveCallCount++
    }
}
