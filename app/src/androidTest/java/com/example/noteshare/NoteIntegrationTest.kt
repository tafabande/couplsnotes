package com.example.noteshare

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.noteshare.model.Note
import com.example.noteshare.repository.NoteRepository
import org.junit.Rule
import org.junit.Test

class FakeNoteRepository : NoteRepository(null) {
    var addNoteCalled = false
    var getNotesCalled = false

    override fun getNotes(onSuccess: (List<Note>) -> Unit, onFailure: (Exception) -> Unit) {
        getNotesCalled = true
        onSuccess(
            listOf(
                Note(id = "1", title = "Fake Note 1", content = "Content 1", author = "Author 1"),
                Note(id = "2", title = "Fake Note 2", content = "Content 2", author = "Author 2")
            )
        )
    }

    override fun addNote(note: Note, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        addNoteCalled = true
        onSuccess()
    }

    override fun deleteNote(noteId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        onSuccess()
    }
}

class NoteIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNoteListAndInsertionFlow() {
        val fakeRepository = FakeNoteRepository()

        composeTestRule.setContent {
            NoteShareScreen(repository = fakeRepository, showToast = {})
        }

        // Verify fake notes are fetched and displayed
        composeTestRule.onNodeWithText("Fake Note 1").assertExists()
        composeTestRule.onNodeWithText("Fake Note 2").assertExists()

        // Interact with input fields
        composeTestRule.onNodeWithText("Title").performTextInput("New Note")
        composeTestRule.onNodeWithText("Content").performTextInput("New Content")
        composeTestRule.onNodeWithText("Author").performTextInput("New Author")

        // Trigger action button
        composeTestRule.onNodeWithText("Add Note").performClick()

        // Assert interaction with fake repository
        assert(fakeRepository.addNoteCalled)
    }
}
