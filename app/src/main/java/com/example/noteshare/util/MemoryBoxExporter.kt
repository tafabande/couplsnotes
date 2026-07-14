package com.example.noteshare.util

import android.content.Context
import android.net.Uri
import com.example.noteshare.data.local.db.dao.EventDao
import com.example.noteshare.data.local.db.dao.MoodDao
import com.example.noteshare.data.local.db.dao.NoteDao
import com.example.noteshare.data.model.SyncStatus
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class MemoryBoxExporter @Inject constructor(
    private val noteDao: NoteDao,
    private val moodDao: MoodDao,
    private val eventDao: EventDao
) {
    /**
     * Exports all local data for the given pairId to a ZIP file via the provided OutputStream.
     * The OutputStream is typically obtained from a ContentResolver using the Uri returned by 
     * ACTION_CREATE_DOCUMENT intent.
     */
    suspend fun exportMemoryBox(
        context: Context,
        pairId: String,
        outputUri: Uri
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val gson = GsonBuilder().setPrettyPrinting().create()

            val notes = noteDao.getVisibleNotes(pairId).first()
            val moods = moodDao.getAllMoods(pairId).first()
            val events = eventDao.getUpcomingEvents(pairId).first()

            val exportedNotes = notes.map { note ->
                if (note.isVault) {
                    try {
                        note.copy(content = EncryptionUtils.decrypt(note.content))
                    } catch (_: Exception) {
                        note
                    }
                } else {
                    note
                }
            }

            val notesJson = gson.toJson(exportedNotes)
            val moodsJson = gson.toJson(moods)
            val eventsJson = gson.toJson(events)

            // Zip the files
            context.contentResolver.openOutputStream(outputUri)?.use { fos ->
                ZipOutputStream(fos).use { zos ->
                    // Notes
                    zos.putNextEntry(ZipEntry("notes.json"))
                    zos.write(notesJson.toByteArray())
                    zos.closeEntry()

                    // Moods
                    zos.putNextEntry(ZipEntry("moods.json"))
                    zos.write(moodsJson.toByteArray())
                    zos.closeEntry()

                    // Events
                    zos.putNextEntry(ZipEntry("events.json"))
                    zos.write(eventsJson.toByteArray())
                    zos.closeEntry()
                    
                    // Markdown Journal
                    zos.putNextEntry(ZipEntry("journal.md"))
                    val mdContent = "# Our Memory Box\n\nA beautiful export of all shared memories.\n"
                    zos.write(mdContent.toByteArray())
                    zos.closeEntry()
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to create Memory Box export: ${e.message}", e)
        }
    }
}
