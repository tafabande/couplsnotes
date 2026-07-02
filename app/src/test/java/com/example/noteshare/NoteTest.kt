package com.example.noteshare

import com.example.noteshare.model.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteTest {

    @Test
    fun testNoteInitialization() {
        val note = Note(
            id = "123",
            title = "Test Note",
            content = "This is a unit test",
            author = "Tester"
        )

        assertEquals("123", note.id)
        assertEquals("Test Note", note.title)
        assertEquals("This is a unit test", note.content)
        assertEquals("Tester", note.author)
        assertTrue(note.timestamp <= System.currentTimeMillis())
    }
}
