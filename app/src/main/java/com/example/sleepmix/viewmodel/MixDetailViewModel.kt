package com.example.sleepmix.viewmodel

import android.annotation.SuppressLint
import com.example.sleepmix.room.MixWithSounds
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepmix.media.MixPlaybackService
import com.example.sleepmix.repositori.MixRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MixDetailUiState(
    val mixWithSounds: MixWithSounds? = null,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val serviceBound: Boolean = false
)

class MixDetailViewModel(
    private val mixRepository: MixRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MixDetailUiState())
    val uiState: StateFlow<MixDetailUiState> = _uiState.asStateFlow()

    @SuppressLint("StaticFieldLeak")
    private var mixPlaybackService: MixPlaybackService? = null
    private var mixId: Int = 0

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("MixDetailVM", "🔗 onServiceConnected called")
            val binder = service as MixPlaybackService.LocalBinder
            mixPlaybackService = binder.getService()
            _uiState.update { it.copy(serviceBound = true) }
            Log.d("MixDetailVM", "✅ Service bound successfully")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w("MixDetailVM", "⚠️ onServiceDisconnected called")
            mixPlaybackService = null
            _uiState.update { it.copy(serviceBound = false) }
        }
    }

    fun loadMix(mixId: Int) {
        this.mixId = mixId
        Log.d("MixDetailVM", "📥 Loading mix: $mixId")

        viewModelScope.launch {
            mixRepository.getMixWithSoundsById(mixId).collect { mixData ->
                if (mixData != null) {
                    Log.d("MixDetailVM", "✅ Mix loaded: ${mixData.mix.mixName}")
                    Log.d("MixDetailVM", "   Sounds in mix: ${mixData.sounds.size}")
                    mixData.sounds.forEach { mixSound ->
                        Log.d("MixDetailVM", "   - SoundId: ${mixSound.soundId}, Volume: ${mixSound.volumeLevel}")
                    }
                } else {
                    Log.e("MixDetailVM", "❌ Mix not found!")
                }

                _uiState.update {
                    it.copy(
                        mixWithSounds = mixData,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun bindService(context: Context) {
        Log.d("MixDetailVM", "🔌 Attempting to bind service...")
        val intent = Intent(context, MixPlaybackService::class.java)

        try {
            context.startService(intent)
            Log.d("MixDetailVM", "✅ Service started")

            val bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            Log.d("MixDetailVM", "Bind result: $bound")
        } catch (e: Exception) {
            Log.e("MixDetailVM", "❌ Error binding service", e)
        }
    }

    fun unbindService(context: Context) {
        if (uiState.value.serviceBound) {
            Log.d("MixDetailVM", "🔌 Unbinding service")
            try {
                context.unbindService(connection)
                _uiState.update { it.copy(serviceBound = false) }
            } catch (e: Exception) {
                Log.e("MixDetailVM", "Error unbinding", e)
            }
        }
    }

    fun togglePlayPause() {
        Log.d("MixDetailVM", "🎵 togglePlayPause called")
        Log.d("MixDetailVM", "   mixWithSounds: ${uiState.value.mixWithSounds?.mix?.mixName}")
        Log.d("MixDetailVM", "   service: ${if (mixPlaybackService != null) "bound" else "NULL"}")
        Log.d("MixDetailVM", "   serviceBound flag: ${uiState.value.serviceBound}")
        Log.d("MixDetailVM", "   isPlaying: ${uiState.value.isPlaying}")

        if (uiState.value.mixWithSounds == null) {
            Log.e("MixDetailVM", "❌ Cannot toggle: mixWithSounds is NULL")
            return
        }

        if (mixPlaybackService == null) {
            Log.e("MixDetailVM", "❌ Cannot toggle: service is NULL")
            Log.e("MixDetailVM", "   Service bound flag: ${uiState.value.serviceBound}")
            return
        }

        if (uiState.value.isPlaying) {
            Log.d("MixDetailVM", "⏸️ Stopping playback")
            mixPlaybackService?.stopAll()
            _uiState.update { it.copy(isPlaying = false) }
        } else {
            Log.d("MixDetailVM", "▶️ Starting playback for mixId: $mixId")
            try {
                mixPlaybackService?.startMix(mixId)
                _uiState.update { it.copy(isPlaying = true) }
                Log.d("MixDetailVM", "✅ startMix() called successfully")
            } catch (e: Exception) {
                Log.e("MixDetailVM", "❌ Error calling startMix()", e)
            }
        }
    }

    override fun onCleared() {
        Log.d("MixDetailVM", "🧹 ViewModel cleared")
        super.onCleared()
    }
}