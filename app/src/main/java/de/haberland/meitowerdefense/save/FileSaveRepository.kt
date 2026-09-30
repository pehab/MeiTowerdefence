package de.haberland.meitowerdefense.save

import android.content.Context

/** Android entry point; the file store preserves a backup and archives damaged saves. */
class FileSaveRepository(context: Context) : SaveRepository by JsonFileSaveRepository(context.filesDir)
