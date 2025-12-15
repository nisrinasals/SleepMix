package com.example.sleepmix.viewmodel

import android.media.MediaPlayer
import android.net.Uri
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

data class BrowseSoundUiState(
    val availableSounds: List<Sound> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val currentlyPlayingId: Int? = null
)

class BrowseSoundViewModel(
    private val soundRepository: SoundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseSoundUiState())
    val uiState: StateFlow<BrowseSoundUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingSound: Sound? = null

    init {
        loadSounds()
    }

    private fun loadSounds() {
        viewModelScope.launch {
            try {
                val sounds = soundRepository.getAllSounds()
                Log.d("BrowseSound", "Loaded ${sounds.size} sounds")
                sounds.forEach { sound ->
                    Log.d("BrowseSound", "Sound: ${sound.name}, Path: ${sound.filePath}, IconRes: ${sound.iconRes}")
                }
                _uiState.update {
                    it.copy(
                        availableSounds = sounds,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("BrowseSound", "Error loading sounds", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load sounds: ${e.message}"
                    )
                }
            }
        }
    }

    fun playPreview(sound: Sound) {
        Log.d("BrowseSound", "playPreview called for: ${sound.name}")

        try {
            // If same sound, toggle play/pause
            if (currentPlayingSound?.soundId == sound.soundId && mediaPlayer != null) {
                if (mediaPlayer?.isPlaying == true) {
                    Log.d("BrowseSound", "Pausing current sound")
                    mediaPlayer?.pause()
                    _uiState.update { it.copy(currentlyPlayingId = null) }
                } else {
                    Log.d("BrowseSound", "Resuming paused sound")
                    mediaPlayer?.start()
                    _uiState.update { it.copy(currentlyPlayingId = sound.soundId) }
                }
                return
            }

            // Stop any currently playing sound
            stopPreview()

            Log.d("BrowseSound", "Creating new MediaPlayer")
            Log.d("BrowseSound", "File path: ${sound.filePath}")

            // METHOD 1: Try MediaPlayer with Uri (more reliable)
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(sound.filePath)

                    setOnPreparedListener { mp ->
                        Log.d("BrowseSound", "MediaPlayer prepared, starting playback")
                        mp.start()
                        _uiState.update { it.copy(currentlyPlayingId = sound.soundId) }
                    }

                    setOnCompletionListener {
                        Log.d("BrowseSound", "Playback completed")
                        _uiState.update { it.copy(currentlyPlayingId = null) }
                        currentPlayingSound = null
                    }

                    setOnErrorListener { mp, what, extra ->
                        Log.e("BrowseSound", "MediaPlayer error: what=$what, extra=$extra")
                        _uiState.update {
                            it.copy(
                                currentlyPlayingId = null,
                                errorMessage = "Error playing sound: Prepare failed : status=0x${Integer.toHexString(extra)}"
                            )
                        }
                        true
                    }

                    prepareAsync()  // Use async prepare
                }

                currentPlayingSound = sound

            } catch (e: Exception) {
                Log.e("BrowseSound", "Method 1 failed, trying method 2", e)

                // METHOD 2: Extract resource ID and use MediaPlayer.create()
                try {
                    // Extract resource ID from path
                    // Format: "android.resource://com.example.sleepmix/2131689472"
                    val resourceId = sound.filePath.substringAfterLast("/").toIntOrNull()

                    if (resourceId != null && resourceId > 0) {
                        Log.d("BrowseSound", "Using resource ID: $resourceId")
                        // Note: This requires Context - we'll need to pass it
                        throw Exception("Need Context for MediaPlayer.create()")
                    } else {
                        throw Exception("Invalid resource ID")
                    }

                } catch (e2: Exception) {
                    Log.e("BrowseSound", "Method 2 also failed", e2)
                    throw e  // Re-throw original exception
                }
            }

        } catch (e: Exception) {
            Log.e("BrowseSound", "Error in playPreview", e)
            _uiState.update {
                it.copy(
                    currentlyPlayingId = null,
                    errorMessage = "Error playing sound: ${e.message}"
                )
            }
        }
    }

    fun stopPreview() {
        try {
            Log.d("BrowseSound", "Stopping preview")
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            currentPlayingSound = null
            _uiState.update { it.copy(currentlyPlayingId = null) }
        } catch (e: Exception) {
            Log.e("BrowseSound", "Error stopping preview", e)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        Log.d("BrowseSound", "ViewModel cleared")
        stopPreview()
        super.onCleared()
    }
}