package com.example.noteshare.ui.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteshare.data.model.MoodEntry
import com.example.noteshare.data.repository.AuthRepository
import com.example.noteshare.data.repository.MoodRepository
import com.example.noteshare.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val moodRepository: MoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState: StateFlow<MoodUiState> = _uiState.asStateFlow()

    init { loadMoods() }

    private fun loadMoods() {
        viewModelScope.launch {
            val user = (authRepository.getCurrentUserProfile() as? Result.Success)?.data ?: return@launch
            val pairId = user.pairId ?: return@launch

            _uiState.update { it.copy(userId = user.id, userName = user.displayName) }

            moodRepository.observeMoods(pairId).collect { moods ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        moods = moods,
                        pairId = pairId
                    )
                }
            }
        }
    }

    fun submitMood(level: Int, note: String?) {
        viewModelScope.launch {
            val pid = _uiState.value.pairId ?: return@launch
            val mood = MoodEntry(
                userId = _uiState.value.userId,
                userName = _uiState.value.userName,
                level = level,
                note = note?.ifBlank { null }
            )
            moodRepository.addMood(pid, mood)
            _uiState.update { it.copy(justSubmitted = true) }
        }
    }
}

data class MoodUiState(
    val isLoading: Boolean = true,
    val moods: List<MoodEntry> = emptyList(),
    val userId: String = "",
    val userName: String = "",
    val pairId: String? = null,
    val justSubmitted: Boolean = false
)
