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

    init {
        loadCurrentPair()
    }

    fun loadCurrentPair() {
        viewModelScope.launch {
            val profile = authRepository.getCurrentUserProfile()
            val user = (profile as? Result.Success)?.data ?: return@launch
            val pairId = user.pairId ?: return@launch
            when (val result = pairRepository.getPair(pairId)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        pair = result.data,
                        isPaired = result.data.isActive,
                        inviteCode = result.data.inviteCode.takeIf { code -> code.isNotBlank() }
                    )
                }
                is Result.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

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
                            isPaired = result.data.isActive
                        )
                    }
                    if (result.data.isActive) {
                        authRepository.updateProfile(mapOf("pairId" to result.data.id))
                        _pairingEvent.emit(PairingEvent.PairSuccess)
                    } else {
                        _pairingEvent.emit(PairingEvent.PairRequested)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }

            }
        }
    }

    fun approveJoinRequest(requesterId: String) {
        viewModelScope.launch {
            val currentPair = _uiState.value.pair ?: return@launch
            val approverId = authRepository.currentUserId ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = pairRepository.approveJoinRequest(currentPair.id, approverId, requesterId)) {
                is Result.Success -> {
                    authRepository.updateProfile(mapOf("pairId" to currentPair.id))
                    _uiState.update { it.copy(isLoading = false, pair = result.data, isPaired = true) }
                    _pairingEvent.emit(PairingEvent.PairSuccess)
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun rejectJoinRequest() {
        viewModelScope.launch {
            val currentPair = _uiState.value.pair ?: return@launch
            val approverId = authRepository.currentUserId ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = pairRepository.rejectJoinRequest(currentPair.id, approverId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, pair = currentPair.copy(pendingJoinUserId = null, pendingJoinUserName = null, joinRequestedAt = null)) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
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

    fun requestDisconnect() {
        viewModelScope.launch {
            val currentPair = _uiState.value.pair ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = pairRepository.requestDisconnect(currentPair.id)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun cancelDisconnect() {
        viewModelScope.launch {
            val currentPair = _uiState.value.pair ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = pairRepository.cancelDisconnect(currentPair.id)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun requestWipe() {
        viewModelScope.launch {
            val currentPair = _uiState.value.pair ?: return@launch
            val requesterId = authRepository.currentUserId ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = pairRepository.requestWipe(currentPair.id, requesterId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun confirmWipe() {
        viewModelScope.launch {
            val currentPair = _uiState.value.pair ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = pairRepository.confirmWipe(currentPair.id)) {
                is Result.Success -> {
                    authRepository.updateProfile(mapOf("pairId" to null))
                    _uiState.update { it.copy(isLoading = false, isPaired = false, pair = currentPair.copy(status = "dissolved")) }
                    _pairingEvent.emit(PairingEvent.PairDissolved)
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
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
    data object PairRequested : PairingEvent()
    data object PairDissolved : PairingEvent()
}
