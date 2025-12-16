package com.example.sleepmix.viewmodel

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepmix.repositori.MixRepository
import com.example.sleepmix.repositori.SoundRepository
import com.example.sleepmix.room.MixSound
import com.example.sleepmix.room.Sound
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditVolumeUiState(
    val mixSound: MixSound? = null,
    val sound: Sound? = null,
    val volumeLevel: Float = 0.5f,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isPlaying: Boolean = false,
    val updateSuccess: Boolean = false,
    val removeSuccess: Boolean = false,
    val errorMessage: String? = null
)

class EditVolumeViewModel(
    private val mixRepository: MixRepository,
    private val soundRepository: SoundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditVolumeUiState())
    val uiState: StateFlow<EditVolumeUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var currentMixId: Int = 0
    private var currentSoundId: Int = 0

    fun loadMixSound(mixId: Int, soundId: Int) {
        currentMixId = mixId
        currentSoundId = soundId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load mix with sounds
                mixRepository.getMixWithSoundsById(mixId).collect { mixWithSounds ->
                    if (mixWithSounds != null) {
                        // Find the specific MixSound
                        val mixSound = mixWithSounds.sounds.find { it.soundId == soundId }

                        if (mixSound != null) {
                            // Load Sound details
                            val sound = soundRepository.getSoundById(soundId)

                            _uiState.update {
                                it.copy(
                                    mixSound = mixSound,
                                    sound = sound,
                                    volumeLevel = mixSound.volumeLevel,
                                    isLoading = false
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Sound not found in mix"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("EditVolumeVM", "Error loading mix sound", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load sound: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateVolumeLevel(newVolume: Float) {
        _uiState.update { it.copy(volumeLevel = newVolume) }
    }

    fun playPreview(sound: Sound, context: Context) {
        Log.d("EditVolumeVM", "playPreview: ${sound.name}")

        try {
            stopPreview()

            val resourceId = sound.filePath.substringAfterLast("/").toIntOrNull()
            if (resourceId == null || resourceId == 0) {
                throw Exception("Invalid resource ID")
            }

            mediaPlayer = MediaPlayer.create(context, resourceId)

            if (mediaPlayer == null) {
                throw Exception("MediaPlayer.create() returned null")
            }

            mediaPlayer?.apply {
                isLooping = true
                setVolume(uiState.value.volumeLevel, uiState.value.volumeLevel)
                start()
            }

            _uiState.update { it.copy(isPlaying = true) }

        } catch (e: Exception) {
            Log.e("EditVolumeVM", "Error playing preview", e)
            _uiState.update {
                it.copy(
                    isPlaying = false,
                    errorMessage = "Error playing sound: ${e.message}"
                )
            }
        }
    }

    fun updatePreviewVolume(newVolume: Float) {
        mediaPlayer?.setVolume(newVolume, newVolume)
    }

    fun stopPreview() {
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
            Log.e("EditVolumeVM", "Error stopping preview", e)
        }
    }

    fun updateSound() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                val mixSound = uiState.value.mixSound ?: throw Exception("MixSound not found")

                // Update volume in database
                val updatedMixSound = mixSound.copy(volumeLevel = uiState.value.volumeLevel)
                mixRepository.updateMixSound(updatedMixSound)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        updateSuccess = true
                    )
                }

            } catch (e: Exception) {
                Log.e("EditVolumeVM", "Error updating sound", e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to update: ${e.message}"
                    )
                }
            }
        }
    }

    fun removeSound() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                // Stop preview if playing
                stopPreview()

                // Get current mixSound
                val mixSound = uiState.value.mixSound ?: throw Exception("MixSound not found")

                // Delete from database
                mixRepository.deleteMixSound(mixSound)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        removeSuccess = true
                    )
                }

            } catch (e: Exception) {
                Log.e("EditVolumeVM", "Error removing sound", e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to remove: ${e.message}"
                    )
                }
            }
        }
    }

    override fun onCleared() {
        stopPreview()
        super.onCleared()
    }
}