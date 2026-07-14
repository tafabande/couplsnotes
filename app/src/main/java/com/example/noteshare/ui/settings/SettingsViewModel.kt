package com.example.noteshare.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteshare.data.model.User
import com.example.noteshare.data.repository.AuthRepository
import com.example.noteshare.data.repository.NoteRepository
import com.example.noteshare.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUserProfile()) {
                is Result.Success -> {
                    val user = result.data
                    val noteCount = if (user.pairId != null) {
                        noteRepository.getNoteCount(user.pairId!!)
                    } else 0

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            noteCount = noteCount
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

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
