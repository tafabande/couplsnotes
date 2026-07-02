package com.example.noteshare

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.noteshare.model.Note
import org.junit.Rule
import org.junit.Test

class NoteUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun noteItemDisplaysCorrectInformation() {
        val note = Note(
            title = "UI Test Title",
            content = "This content should be displayed",
            author = "UI Designer"
        )

        composeTestRule.setContent {
            NoteItem(note = note, onDelete = {})
        }

        // Verify elements exist on screen
        composeTestRule.onNodeWithText("UI Test Title").assertExists()
        composeTestRule.onNodeWithText("This content should be displayed").assertExists()
        composeTestRule.onNodeWithText("By: UI Designer").assertExists()
    }
}
