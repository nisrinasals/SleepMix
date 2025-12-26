package com.example.sleepmix.viewmodel

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepmix.repositori.SoundRepository
import com.example.sleepmix.room.Sound
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SoundDetailUiState(
    val sound: Sound? = null,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val volumeLevel: Float = 0.5f,
    val errorMessage: String? = null
)

class SoundDetailViewModel(
    private val soundRepository: SoundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoundDetailUiState())
    val uiState: StateFlow<SoundDetailUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    fun loadSound(soundId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val sound = soundRepository.getSoundById(soundId)

                _uiState.update {
                    it.copy(
                        sound = sound,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                Log.e("SoundDetailVM", "Error loading sound", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load sound: ${e.message}"
                    )
                }
            }
        }
    }

    fun playSound(sound: Sound, context: Context) {
        Log.d("SoundDetailVM", "playSound: ${sound.name}")

        try {
            // If already playing, just resume
            if (mediaPlayer != null) {
                mediaPlayer?.start()
                _uiState.update { it.copy(isPlaying = true) }
                return
            }

            // Extract resource ID from path
            val resourceId = sound.filePath.substringAfterLast("/").toIntOrNull()

            if (resourceId == null || resourceId == 0) {
                throw Exception("Invalid resource ID from path: ${sound.filePath}")
            }

            Log.d("SoundDetailVM", "Using resource ID: $resourceId")

            // Create MediaPlayer
            mediaPlayer = MediaPlayer.create(context, resourceId)

            if (mediaPlayer == null) {
                throw Exception("MediaPlayer.create() returned null for resource: $resourceId")
            }

            mediaPlayer?.apply {
                isLooping = true
                setVolume(uiState.value.volumeLevel, uiState.value.volumeLevel)

                setOnErrorListener { _, what, extra ->
                    Log.e("SoundDetailVM", "MediaPlayer error: what=$what, extra=$extra")
                    _uiState.update {
                        it.copy(
                            isPlaying = false,
                            errorMessage = "Error playing sound"
                        )
                    }
                    true
                }

                start()
            }

            _uiState.update { it.copy(isPlaying = true) }
            Log.d("SoundDetailVM", "Sound started playing")

        } catch (e: Exception) {
            Log.e("SoundDetailVM", "Error playing sound", e)
            _uiState.update {
                it.copy(
                    isPlaying = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    fun pauseSound() {
        try {
            mediaPlayer?.pause()
            _uiState.update { it.copy(isPlaying = false) }
        } catch (e: Exception) {
            Log.e("SoundDetailVM", "Error pausing sound", e)
        }
    }

    fun stopSound() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            _uiState.update { it.copy(isPlaying = false) }
        } catch (e: Exception) {
            Log.e("SoundDetailVM", "Error stopping sound", e)
        }
    }

    fun updateVolume(newVolume: Float) {
        _uiState.update { it.copy(volumeLevel = newVolume) }
        mediaPlayer?.setVolume(newVolume, newVolume)
    }

    override fun onCleared() {
        Log.d("SoundDetailVM", "🧹 onCleared - cleaning up")
        stopSound()  // ✅ Releases MediaPlayer
        super.onCleared()
    }
}