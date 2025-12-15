package com.example.sleepmix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepmix.repositori.UserRepository
import com.example.sleepmix.room.User
import com.example.sleepmix.util.PasswordHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistrasiUiState(
    val nama: String = "",
    val email: String = "",
    val passwordInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registrasiSuccess: Boolean = false
)

class RegistrasiViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrasiUiState())
    val uiState: StateFlow<RegistrasiUiState> = _uiState.asStateFlow()

    // Handlers untuk input UI
    fun updateNama(nama: String) {
        _uiState.update { it.copy(nama = nama) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(passwordInput = password) }
    }

    // Fungsi utama untuk proses registrasi (sesuai Activity Diagram Registrasi)
    fun attemptRegistration() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val state = uiState.value

            // 1. Validasi Input
            if (state.nama.isBlank() || state.email.isBlank() || state.passwordInput.length < 6) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Data input tidak valid. Pastikan password minimal 6 karakter."
                    )
                }
                return@launch
            }

            // 2. Cek Email Sudah Ada?
            val existingUser = userRepository.getUserByEmail(state.email)

            if (existingUser != null) {
                // Email sudah terdaftar
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Email ${state.email} sudah terdaftar. Silakan login."
                    )
                }
                return@launch
            }

            // 3. Hash Password menggunakan PasswordHasher
            val hashedPassword = PasswordHasher.hashPassword(state.passwordInput)

            // 4. INSERT INTO USER Table
            val newUser = User(
                nama = state.nama,
                email = state.email,
                passwordHash = hashedPassword,
                isLoggedIn = false
            )

            try {
                userRepository.insertUser(newUser)

                // 5. Success
                _uiState.update { it.copy(isLoading = false, registrasiSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Gagal mendaftar: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}