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

    /**
     * Play preview dengan proper resource loading
     * Requires Context to use MediaPlayer.create()
     */
    fun playPreview(sound: Sound, context: Context) {
        Log.d("BrowseSound", "playPreview: ${sound.name}, path: ${sound.filePath}")

        try {
            // Toggle if same sound
            if (currentPlayingSound?.soundId == sound.soundId && mediaPlayer != null) {
                if (mediaPlayer?.isPlaying == true) {
                    Log.d("BrowseSound", "Pausing")
                    mediaPlayer?.pause()
                    _uiState.update { it.copy(currentlyPlayingId = null) }
                } else {
                    Log.d("BrowseSound", "Resuming")
                    mediaPlayer?.start()
                    _uiState.update { it.copy(currentlyPlayingId = sound.soundId) }
                }
                return
            }

            // Stop previous
            stopPreview()

            // Extract resource ID from path
            // Format: "android.resource://com.example.sleepmix/2131689472"
            val resourceId = sound.filePath.substringAfterLast("/").toIntOrNull()

            if (resourceId == null || resourceId == 0) {
                throw Exception("Invalid resource ID from path: ${sound.filePath}")
            }

            Log.d("BrowseSound", "Using resource ID: $resourceId")

            // Use MediaPlayer.create() - more reliable for resources
            mediaPlayer = MediaPlayer.create(context, resourceId)

            if (mediaPlayer == null) {
                throw Exception("MediaPlayer.create() returned null for resource: $resourceId")
            }

            mediaPlayer?.apply {
                setOnCompletionListener {
                    Log.d("BrowseSound", "Playback completed")
                    _uiState.update { it.copy(currentlyPlayingId = null) }
                    currentPlayingSound = null
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("BrowseSound", "MediaPlayer error: what=$what, extra=$extra")
                    _uiState.update {
                        it.copy(
                            currentlyPlayingId = null,
                            errorMessage = "Error: Cannot play sound"
                        )
                    }
                    true
                }

                start()
                Log.d("BrowseSound", "Started playing")
            }

            currentPlayingSound = sound
            _uiState.update { it.copy(currentlyPlayingId = sound.soundId) }

        } catch (e: Exception) {
            Log.e("BrowseSound", "Error in playPreview", e)
            _uiState.update {
                it.copy(
                    currentlyPlayingId = null,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
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
            currentPlayingSound = null
            _uiState.update { it.copy(currentlyPlayingId = null) }
        } catch (e: Exception) {
            Log.e("BrowseSound", "Error stopping", e)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        stopPreview()
        super.onCleared()
    }
}