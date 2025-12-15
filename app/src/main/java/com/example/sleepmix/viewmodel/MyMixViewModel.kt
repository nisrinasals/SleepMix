package com.example.sleepmix.viewmodel

import com.example.sleepmix.room.MixWithSounds
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepmix.repositori.MixRepository
import com.example.sleepmix.repositori.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

data class MyMixUiState(
    val userMixes: List<MixWithSounds> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false // Status untuk menampilkan "Belum ada mix"
)

class MyMixViewModel(
    private val mixRepository: MixRepository,
    private val userRepository: UserRepository // Diperlukan untuk mendapatkan currentUserId
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MyMixUiState> = userRepository.currentUserId // Ambil ID dari SessionManager
        .flatMapLatest { userId ->
            if (userId != null) {
                // Ambil Mix dari Repository jika user ID ada
                mixRepository.getMixesByUserIdStream(userId)
            } else {
                // Jika tidak ada user ID, kembalikan Flow kosong (atau handle error)
                flowOf(emptyList())
            }
        }
        .map { mixList ->
            MyMixUiState(
                userMixes = mixList,
                isLoading = false,
                isEmpty = mixList.isEmpty() // Memenuhi logika 'Has mixes? No -> Display empty state'
            )
        }
        .catch { e ->
            emit(MyMixUiState(
                isLoading = false,
                errorMessage = "Gagal memuat Mix: ${e.message}",
                isEmpty = true
            ))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MyMixUiState(isLoading = true)
        )

    /**
     * Menghapus Mix dari database (sesuai Activity Diagram Delete Mix).
     */
    fun deleteMix(mixId: Int) = viewModelScope.launch {
        // Cari objek Mix yang akan dihapus dari data lokal
        val mixToDelete = uiState.value.userMixes.find { it.mix.mixId == mixId }?.mix

        if (mixToDelete != null) {
            try {
                // MixRepository akan menjalankan transaksi DELETE di tblMix dan tblMixSound.
                mixRepository.deleteMix(mixToDelete)
            } catch (e: Exception) {
                // Di sini Anda bisa memperbarui state untuk menampilkan Toast/Snackbar
                // Misalnya: _uiState.update { it.copy(errorMessage = "Gagal menghapus Mix.") }
            }
        }
    }
    fun logout() = viewModelScope.launch {
        userRepository.clearUserSession()
    }
}