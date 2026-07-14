package com.example.noteshare.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteshare.data.model.Note
import com.example.noteshare.data.model.Tag
import com.example.noteshare.data.repository.AuthRepository
import com.example.noteshare.data.repository.NoteRepository
import com.example.noteshare.util.EncryptionUtils
import com.example.noteshare.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _noteEvent = MutableSharedFlow<NoteEvent>()
    val noteEvent: SharedFlow<NoteEvent> = _noteEvent.asSharedFlow()

    private var pairId: String? = null

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userResult = authRepository.getCurrentUserProfile()
            val user = (userResult as? Result.Success)?.data ?: return@launch
            pairId = user.pairId ?: return@launch

            noteRepository.observeNotes(pairId!!).collect { notes ->
                val filtered = filterNotes(notes, _uiState.value.selectedTag, _uiState.value.searchQuery)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allNotes = notes.filter { n -> !n.isDeleted && !n.isArchived },
                        filteredNotes = filtered,
                        userId = user.id,
                        userName = user.displayName
                    )
                }
                noteRepository.syncToLocal(notes)
            }
        }
    }

    fun search(query: String) {
        _uiState.update {
            val filtered = filterNotes(it.allNotes, it.selectedTag, query)
            it.copy(searchQuery = query, filteredNotes = filtered)
        }
    }

    fun filterByTag(tag: String?) {
        _uiState.update {
            val filtered = filterNotes(it.allNotes, tag, it.searchQuery)
            it.copy(selectedTag = tag, filteredNotes = filtered)
        }
    }

    private fun filterNotes(notes: List<Note>, tag: String?, query: String): List<Note> {
        var result = notes.filter { !it.isDeleted && !it.isArchived }
        if (tag != null) {
            result = result.filter { it.tags.contains(tag) }
        }
        if (query.isNotBlank()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }
        return result.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
    }

    fun createNote(title: String, content: String, tags: List<String>, displayStyle: String, isVault: Boolean = false) {
        viewModelScope.launch {
            val pid = pairId ?: return@launch
            val userId = authRepository.currentUserId ?: return@launch
            val userName = _uiState.value.userName

            val contentToSave = if (isVault) EncryptionUtils.encrypt(content) else content

            val note = Note(
                title = title,
                content = contentToSave,
                authorId = userId,
                authorName = userName,
                tags = tags,
                displayStyle = displayStyle,
                isVault = isVault
            )
            when (val result = noteRepository.createNote(pid, note)) {
                is Result.Success -> _noteEvent.emit(NoteEvent.NoteSaved)
                is Result.Error -> _noteEvent.emit(NoteEvent.Error(result.message))
                is Result.Loading -> {}
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            val pid = pairId ?: return@launch
            noteRepository.deleteNote(pid, noteId)
        }
    }

    fun togglePin(noteId: String, isPinned: Boolean) {
        viewModelScope.launch {
            val pid = pairId ?: return@launch
            noteRepository.togglePin(pid, noteId, isPinned)
        }
    }

    fun archiveNote(noteId: String) {
        viewModelScope.launch {
            val pid = pairId ?: return@launch
            noteRepository.archiveNote(pid, noteId)
        }
    }
}

data class NotesUiState(
    val isLoading: Boolean = false,
    val allNotes: List<Note> = emptyList(),
    val filteredNotes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val selectedTag: String? = null,
    val userId: String = "",
    val userName: String = "",
    val error: String? = null
)

sealed class NoteEvent {
    data object NoteSaved : NoteEvent()
    data class Error(val message: String) : NoteEvent()
}
