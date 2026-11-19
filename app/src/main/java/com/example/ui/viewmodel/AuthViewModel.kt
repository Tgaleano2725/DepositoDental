package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.InventoryRepository
import com.example.security.InputValidator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val failedAttempts: Int = 0,
    val lockoutSecondsRemaining: Int = 0,
    val authenticatedUser: String? = null
) {
    val isLockedOut: Boolean get() = lockoutSecondsRemaining > 0
    val isAuthenticated: Boolean get() = authenticatedUser != null
    val remainingAttemptsBeforeLockout: Int get() = (MAX_FAILED_ATTEMPTS - failedAttempts).coerceAtLeast(0)

    companion object {
        const val MAX_FAILED_ATTEMPTS = 5
    }
}

class AuthViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var lockoutJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeDatabaseIfNeeded()
        }
    }

    fun onUsernameChange(value: String) {
        val cleaned = value.take(32)
        _uiState.update {
            it.copy(
                username = cleaned,
                usernameError = null,
                generalError = null
            )
        }
    }

    fun onPasswordChange(value: String) {
        val cleaned = value.take(64)
        _uiState.update {
            it.copy(
                password = cleaned,
                passwordError = null,
                generalError = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun submitLogin() {
        val current = _uiState.value
        if (current.isLoading || current.isLockedOut) return

        val validation = InputValidator.validateLogin(current.username, current.password)
        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    usernameError = validation.usernameError,
                    passwordError = validation.passwordError,
                    generalError = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            val authenticated = repository.authenticate(current.username, current.password)
            if (authenticated) {
                lockoutJob?.cancel()
                _uiState.update {
                    AuthUiState(authenticatedUser = current.username.trim())
                }
            } else {
                val nextAttempts = current.failedAttempts + 1
                if (nextAttempts >= AuthUiState.MAX_FAILED_ATTEMPTS) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            failedAttempts = nextAttempts,
                            password = "",
                            generalError = "Acceso bloqueado temporalmente por seguridad."
                        )
                    }
                    startLockoutCountdown()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            failedAttempts = nextAttempts,
                            generalError = "Credenciales incorrectas. Verifica usuario y contraseña."
                        )
                    }
                }
            }
        }
    }

    fun logout() {
        lockoutJob?.cancel()
        _uiState.value = AuthUiState()
    }

    private fun startLockoutCountdown() {
        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            for (seconds in LOCKOUT_DURATION_SECONDS downTo 1) {
                _uiState.update { it.copy(lockoutSecondsRemaining = seconds) }
                delay(1_000L)
            }
            _uiState.update {
                it.copy(
                    lockoutSecondsRemaining = 0,
                    failedAttempts = 0,
                    generalError = null
                )
            }
        }
    }

    companion object {
        private const val LOCKOUT_DURATION_SECONDS = 30

        fun factory(repository: InventoryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(repository) as T
                }
            }
    }
}
