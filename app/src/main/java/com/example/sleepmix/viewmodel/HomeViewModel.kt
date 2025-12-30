package com.example.sleepmix.viewmodel

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

/**
 * UI State untuk Home Screen (Sound Library)
 * Sesuai SRS Section 4.1 Screen 2
 */
data class HomeUiState(
    val sounds: List<Sound> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk Home Screen
 * Handles loading sounds untuk Sound Library
 */
class HomeViewModel(
    private val soundRepository: SoundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSounds()
    }

    /**
     * Load semua sounds dari database
     * Sesuai SRS REQ-1.1: minimal 8 jenis suara alam berbeda
     */
    private fun loadSounds() {
        viewModelScope.launch {
            try {
                val sounds = soundRepository.getAllSounds()
                Log.d("HomeViewModel", "Loaded ${sounds.size} sounds")

                _uiState.update {
                    it.copy(
                        sounds = sounds,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading sounds", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load sounds: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
