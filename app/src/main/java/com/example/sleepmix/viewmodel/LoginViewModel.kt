package com.example.sleepmix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepmix.repositori.UserRepository
import com.example.sleepmix.util.PasswordHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val passwordInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false,
    val loggedInUserId: Int? = null
)

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(passwordInput = password) }
    }

    // Fungsi utama untuk proses login (sesuai Activity Diagram Login)
    fun attemptLogin() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val email = uiState.value.email
            val password = uiState.value.passwordInput

            // 1. Ambil User berdasarkan Email
            val user = userRepository.getUserByEmail(email)

            if (user == null) {
                // User tidak ditemukan
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Email tidak terdaftar."
                    )
                }
                return@launch
            }

            // 2. Verifikasi Password menggunakan PasswordHasher
            val isPasswordCorrect = PasswordHasher.verifyPassword(password, user.passwordHash)

            if (isPasswordCorrect) {
                // Password benar - Login berhasil

                // 3. Pastikan semua user lain logout, lalu update status login user ini
                userRepository.logoutAll()
                userRepository.updateUser(user.copy(isLoggedIn = true))
                userRepository.saveSession(user.userId)

                // 4. Success
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginSuccess = true,
                        loggedInUserId = user.userId
                    )
                }
            } else {
                // Password salah
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Password salah. Coba lagi."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}