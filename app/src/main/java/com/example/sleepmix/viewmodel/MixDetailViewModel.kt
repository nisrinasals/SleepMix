package com.example.sleepmix.viewmodel

import com.example.sleepmix.room.MixWithSounds
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepmix.media.MixPlaybackService
import com.example.sleepmix.repositori.MixRepository

import com.example.sleepmix.room.MixSound
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MixDetailUiState(
    val mixWithSounds: MixWithSounds? = null,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false, // Status pemutaran Mix
    val serviceBound: Boolean = false // Status koneksi ke Service
)

class MixDetailViewModel(
    private val mixRepository: MixRepository
) : ViewModel() {

    // State UI
    private val _uiState = MutableStateFlow(MixDetailUiState())
    val uiState: StateFlow<MixDetailUiState> = _uiState.asStateFlow()

    // Service Audio
    private var mixPlaybackService: MixPlaybackService? = null

    // ID Mix yang sedang di-load
    private var mixId: Int = 0

    // Service Connection untuk mengikat ke Service
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MixPlaybackService.LocalBinder
            mixPlaybackService = binder.getService()
            _uiState.update { it.copy(serviceBound = true) }
            // PENTING: Jika Mix sedang dimainkan, status isPlaying harus disinkronkan di sini.
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mixPlaybackService = null
            _uiState.update { it.copy(serviceBound = false) }
        }
    }

    // Dipanggil saat MixDetailScreen pertama kali dibuat
    fun loadMix(mixId: Int) {
        this.mixId = mixId
        // Memuat data Mix secara reaktif dari Room
        viewModelScope.launch {
            mixRepository.getMixWithSoundsById(mixId).collect { mixData ->
                _uiState.update {
                    it.copy(
                        mixWithSounds = mixData,
                        isLoading = false
                    )
                }
            }
        }
    }

    // Dipanggil dari Composable/Activity untuk memulai koneksi ke Service
    fun bindService(context: Context) {
        val intent = Intent(context, MixPlaybackService::class.java)
        // Memulai Service agar tetap berjalan saat Activity/Fragment di-unbind
        context.startService(intent)
        // Mengikat ke Service
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    // Dipanggil dari Composable/Activity saat layar dihancurkan
    fun unbindService(context: Context) {
        if (uiState.value.serviceBound) {
            context.unbindService(connection)
            _uiState.update { it.copy(serviceBound = false) }
        }
    }

    // Aksi: Memulai / Menghentikan Mix
    fun togglePlayPause() {
        if (uiState.value.mixWithSounds == null || mixPlaybackService == null) return

        if (uiState.value.isPlaying) {
            // Hentikan pemutaran
            mixPlaybackService?.stopAll() // Anggap ada fungsi stopAll di Service
            _uiState.update { it.copy(isPlaying = false) }
        } else {
            // Mulai pemutaran Mix
            mixPlaybackService?.startMix(mixId)
            _uiState.update { it.copy(isPlaying = true) }
        }
    }

    // Aksi: Menyesuaikan Volume (Dipanggil dari Slider UI)
    fun updateMixSoundVolume(mixSound: MixSound, newVolume: Float) = viewModelScope.launch {
        // 1. Kirim perintah volume real-time ke Service
        mixPlaybackService?.setSoundVolume(mixSound, newVolume)

        // 2. Simpan volume baru ke Database (Persistence)
        // **PERLU: Tambahkan fungsi updateMixSound(MixSound) di MixRepository**
        val updatedMixSound = mixSound.copy(volumeLevel = newVolume)
        // mixRepository.updateMixSound(updatedMixSound)

        // Untuk sementara, kita hanya update data lokal (UI State) sampai repository diperbarui:
        _uiState.update { currentState ->
            val updatedSounds = currentState.mixWithSounds?.sounds?.map { s ->
                if (s.mixSoundId == mixSound.mixSoundId) updatedMixSound else s
            }
            currentState.copy(
                mixWithSounds = currentState.mixWithSounds?.copy(sounds = updatedSounds ?: emptyList())
            )
        }
    }

    override fun onCleared() {
        // Logika untuk memastikan service di-stop jika ViewModel dibersihkan (opsional)
        // mixPlaybackService?.stopSelf()
        super.onCleared()
    }
}