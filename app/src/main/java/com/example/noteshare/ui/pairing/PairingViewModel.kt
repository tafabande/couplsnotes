package com.example.noteshare.ui.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteshare.data.model.Pair
import com.example.noteshare.data.repository.AuthRepository
import com.example.noteshare.data.repository.PairRepository
import com.example.noteshare.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val pairRepository: PairRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    private val _pairingEvent = MutableSharedFlow<PairingEvent>()
    val pairingEvent: SharedFlow<PairingEvent> = _pairingEvent.asSharedFlow()

    /**
     * Generate an invite code and create a pair.
     */
    fun createInvite() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.currentUserId ?: return@launch

            when (val result = pairRepository.createPair(userId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            inviteCode = result.data.inviteCode,
                            pair = result.data
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }

            }
        }
    }

    /**
     * Join a pair using an invite code.
     */
    fun joinWithCode(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.currentUserId ?: return@launch

            when (val result = pairRepository.joinPair(userId, code.uppercase())) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pair = result.data,
                            isPaired = true
                        )
                    }
                    // Update user's pairId in auth
                    authRepository.updateProfile(mapOf("pairId" to result.data.id))
                    _pairingEvent.emit(PairingEvent.PairSuccess)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }

            }
        }
    }

    /**
     * Observe the pair for when a partner joins (waiting state).
     */
    fun observePairStatus(pairId: String) {
        viewModelScope.launch {
            pairRepository.observePair(pairId).collect { pair ->
                if (pair != null && pair.isActive) {
                    _uiState.update { it.copy(isPaired = true, pair = pair) }
                    _pairingEvent.emit(PairingEvent.PartnerJoined)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class PairingUiState(
    val isLoading: Boolean = false,
    val isPaired: Boolean = false,
    val inviteCode: String? = null,
    val pair: Pair? = null,
    val error: String? = null
)

sealed class PairingEvent {
    data object PairSuccess : PairingEvent()
    data object PartnerJoined : PairingEvent()
}
