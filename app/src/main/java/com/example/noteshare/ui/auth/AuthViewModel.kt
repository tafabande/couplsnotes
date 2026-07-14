package com.example.noteshare.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteshare.data.model.User
import com.example.noteshare.data.repository.AuthRepository
import com.example.noteshare.data.repository.PairRepository
import com.example.noteshare.util.Result
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val pairRepository: PairRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent: SharedFlow<AuthEvent> = _authEvent.asSharedFlow()

    val isLoggedIn: Boolean
        get() = authRepository.isLoggedIn

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { firebaseUser ->
                if (firebaseUser != null) {
                    loadUserProfile()
                } else {
                    _uiState.update { it.copy(isLoggedIn = false, user = null, pairId = null) }
                }
            }
        }
    }

    private suspend fun loadUserProfile() {
        when (val result = authRepository.getCurrentUserProfile()) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        user = result.data,
                        pairId = result.data.pairId,
                        isPaired = result.data.pairId != null
                    )
                }
            }
            is Result.Error -> {
                _uiState.update { it.copy(isLoggedIn = true, error = result.message) }
            }
            is Result.Loading -> {}
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            user = result.data,
                            pairId = result.data.pairId,
                            isPaired = result.data.pairId != null
                        )
                    }
                    if (result.data.pairId != null) {
                        _authEvent.emit(AuthEvent.NavigateToHome)
                    } else {
                        _authEvent.emit(AuthEvent.NavigateToPairing)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signInWithEmail(email, password)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            user = result.data,
                            pairId = result.data.pairId,
                            isPaired = result.data.pairId != null
                        )
                    }
                    if (result.data.pairId != null) {
                        _authEvent.emit(AuthEvent.NavigateToHome)
                    } else {
                        _authEvent.emit(AuthEvent.NavigateToPairing)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.createAccount(email, password, displayName)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            user = result.data
                        )
                    }
                    _authEvent.emit(AuthEvent.NavigateToPairing)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.sendPasswordReset(email)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _authEvent.emit(AuthEvent.PasswordResetSent)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.update { AuthUiState() }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isPaired: Boolean = false,
    val user: User? = null,
    val pairId: String? = null,
    val error: String? = null
)

sealed class AuthEvent {
    data object NavigateToHome : AuthEvent()
    data object NavigateToPairing : AuthEvent()
    data object PasswordResetSent : AuthEvent()
}
