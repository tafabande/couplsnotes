package com.example.noteshare.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteshare.data.model.Note
import com.example.noteshare.data.repository.NoteRepository
import com.example.noteshare.util.EncryptionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    fun loadNote(noteId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            noteRepository.observeNoteById(noteId).collect { note ->
                if (note != null) {
                    val contentToDisplay = if (note.isVault) {
                        try {
                            EncryptionUtils.decrypt(note.content)
                        } catch (e: Exception) {
                            "Error decrypting note."
                        }
                    } else {
                        note.content
                    }
                    val decryptedNote = note.copy(content = contentToDisplay)
                    _uiState.update { it.copy(isLoading = false, note = decryptedNote) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Note not found") }
                }
            }
        }
    }
}

data class NoteDetailUiState(
    val isLoading: Boolean = true,
    val note: Note? = null,
    val error: String? = null
)
