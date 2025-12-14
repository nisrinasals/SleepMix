package com.example.sleepmix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepmix.repositori.UserRepository
import com.example.sleepmix.room.User // Pastikan ini diimpor
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
                _uiState.update { it.copy(isLoading = false, errorMessage = "Data input tidak valid. Pastikan password minimal 6 karakter.") }
                return@launch
            }

            // 2. Cek Email Sudah Ada? (Email Already Exist?)
            val existingUser = userRepository.getUserByEmail(state.email)

            if (existingUser != null) {
                // Yes (Email Already Exist) -> Show Error Message
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Email ${state.email} sudah terdaftar. Silakan login."
                    )
                }
                return@launch
            }

            // 3. Hash Password
            // *PENTING: Gunakan fungsi hash yang aman di sini (misalnya BCrypt)*
            val hashedPassword = hashPassword(state.passwordInput)

            // 4. INSERT INTO USER Table
            val newUser = User(
                nama = state.nama,
                email = state.email,
                passwordHash = hashedPassword, // Menggunakan hash
                isLoggedIn = false // Awalnya tidak login
            )

            try {
                userRepository.insertUser(newUser)

                // 5. Success
                _uiState.update { it.copy(isLoading = false, registrasiSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal mendaftar: ${e.message}") }
            }
        }
    }
}

// *** Placeholder untuk fungsi hashing. Anda harus mengganti ini dengan implementasi hash yang aman (seperti BCrypt).
private fun hashPassword(password: String): String {
    // Implementasi hash yang aman harus ada di sini.
    // Sementara, kita asumsikan input = hash (HANYA UNTUK TESTING LOKAL)
    return password
}