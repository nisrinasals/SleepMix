package com.example.sleepmix.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepmix.repositori.UserRepository
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
    val loginSuccess: Boolean = false
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
                // User Found & Password Match? No (email tidak ditemukan)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Email tidak terdaftar."
                    )
                }
                return@launch
            }

            // 2. Hash Password (Asumsi: Anda memiliki fungsi utilitas hashing)
            // *PENTING: Fungsi hash harus ditambahkan di project Anda*
            val hashedPassword = hashPassword(password)

            // 3. Verifikasi Password (user.passwordHash adalah password hash yang disimpan di DB)
            if (user.passwordHash == hashedPassword) {
                // User Found & Password Match? Yes

                // 4. Pastikan semua user lain logout, lalu update status login user ini.
                userRepository.logoutAll() // Query: UPDATE tblUser SET isLoggedIn = 0
                userRepository.updateUser(user.copy(isLoggedIn = true))

                // 5. Success
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            } else {
                // User Found & Password Match? No (password salah)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Password salah. Coba lagi."
                    )
                }
            }
        }
    }
}

// *** Placeholder untuk fungsi hashing. Anda harus mengganti ini dengan implementasi hash yang aman (misalnya BCrypt).
private fun hashPassword(password: String): String {
    // Implementasi hash yang aman harus ada di sini.
    // Sementara, kita asumsikan input = hash (HANYA UNTUK TESTING LOKAL)
    return password
}