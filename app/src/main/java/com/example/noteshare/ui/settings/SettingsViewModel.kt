package com.example.noteshare.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteshare.data.model.User
import com.example.noteshare.data.repository.AuthRepository
import com.example.noteshare.data.repository.NoteRepository
import com.example.noteshare.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.getCurrentUserProfile()) {
                is Result.Success -> {
                    val user = result.data
                    val noteCount = user.pairId?.let { pairId ->
                        noteRepository.getLocalNotes(pairId).firstOrNull()?.count { note -> !note.isArchived && !note.isDeleted } ?: 0
                    } ?: 0
                    _uiState.update {
                        it.copy(isLoading = false, user = user, noteCount = noteCount)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun refresh() = loadProfile()

    fun updateProfile(fields: Map<String, Any?>) {
        viewModelScope.launch {
            authRepository.updateProfile(fields)
            loadProfile()
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}

data class SettingsUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val noteCount: Int = 0,
    val error: String? = null
)
