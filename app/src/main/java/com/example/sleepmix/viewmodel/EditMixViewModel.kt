package com.example.sleepmix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepmix.repositori.MixRepository
import com.example.sleepmix.repositori.SoundRepository
import com.example.sleepmix.room.Mix
import com.example.sleepmix.room.MixSound
import com.example.sleepmix.room.Sound
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State untuk Edit Mix Screen (PAGE8)
 */
data class EditMixUiState(
    val originalMixId: Int = 0,
    val availableSounds: List<Sound> = emptyList(),
    val selectedMixSounds: List<SelectedMixSound> = emptyList(),
    val mixNameInput: String = "",
    val userId: Int = 0,
    val creationDate: Long = 0,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk PAGE8: Edit Mix Screen
 * Sesuai SRS Section 3.4 MyMix Management
 */
class EditMixViewModel(
    private val mixRepository: MixRepository,
    private val soundRepository: SoundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditMixUiState())
    val uiState: StateFlow<EditMixUiState> = _uiState.asStateFlow()

    fun loadMix(mixId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load all available sounds
                val sounds = soundRepository.getAllSounds()

                // Load the mix data
                mixRepository.getMixWithSoundsById(mixId).collect { mixWithSounds ->
                    if (mixWithSounds != null) {
                        val selectedSounds = mixWithSounds.sounds.map { mixSound ->
                            val sound = sounds.find { it.soundId == mixSound.soundId }
                            SelectedMixSound(
                                soundId = mixSound.soundId,
                                name = sound?.name ?: "Unknown",
                                iconRes = sound?.iconRes ?: android.R.drawable.ic_menu_help,
                                volumeLevel = mixSound.volumeLevel
                            )
                        }

                        _uiState.update {
                            it.copy(
                                originalMixId = mixWithSounds.mix.mixId,
                                availableSounds = sounds,
                                selectedMixSounds = selectedSounds,
                                mixNameInput = mixWithSounds.mix.mixName,
                                userId = mixWithSounds.mix.userId,
                                creationDate = mixWithSounds.mix.creationDate,
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load mix: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Update mix name
     * REQ-4.3: maksimal 30 karakter
     */
    fun updateMixName(name: String) {
        _uiState.update { it.copy(mixNameInput = name, errorMessage = null) }
    }

    /**
     * Toggle sound selection
     * REQ-2.4: Prevent duplicate
     */
    fun toggleSoundSelection(sound: Sound) {
        _uiState.update { currentState ->
            val isSelected = currentState.selectedMixSounds.any { it.soundId == sound.soundId }

            val newSelectedSounds = if (isSelected) {
                currentState.selectedMixSounds.filter { it.soundId != sound.soundId }
            } else {
                // REQ-2.1: Max 5 sounds
                if (currentState.selectedMixSounds.size >= 5) {
                    return@update currentState.copy(
                        errorMessage = "Maksimal 5 suara per mix"
                    )
                }
                currentState.selectedMixSounds + SelectedMixSound(
                    soundId = sound.soundId,
                    name = sound.name,
                    iconRes = sound.iconRes,
                    volumeLevel = 0.5f
                )
            }
            currentState.copy(selectedMixSounds = newSelectedSounds, errorMessage = null)
        }
    }

    /**
     * Add sound dengan volume tertentu (dari PAGE11)
     * REQ-2.4: Prevent duplicate
     * REQ-2.1: Max 5 sounds
     */
    fun addSoundWithVolume(soundId: Int, volumeLevel: Float) {
        viewModelScope.launch {
            val sound = soundRepository.getSoundById(soundId) ?: return@launch

            _uiState.update { currentState ->
                // Check duplicate
                if (currentState.selectedMixSounds.any { it.soundId == soundId }) {
                    return@update currentState.copy(
                        errorMessage = "Suara sudah ada dalam mix"
                    )
                }

                // Check max limit
                if (currentState.selectedMixSounds.size >= 5) {
                    return@update currentState.copy(
                        errorMessage = "Maksimal 5 suara per mix"
                    )
                }

                val newSelectedSounds = currentState.selectedMixSounds + SelectedMixSound(
                    soundId = sound.soundId,
                    name = sound.name,
                    iconRes = sound.iconRes,
                    volumeLevel = volumeLevel
                )

                currentState.copy(selectedMixSounds = newSelectedSounds, errorMessage = null)
            }
        }
    }

    /**
     * Update volume untuk sound yang dipilih
     * REQ-3.1: independent volume control 0-100%
     */
    fun updateSelectedSoundVolume(soundId: Int, newVolume: Float) {
        _uiState.update { currentState ->
            val updatedSounds = currentState.selectedMixSounds.map { selectedSound ->
                if (selectedSound.soundId == soundId) {
                    selectedSound.copy(volumeLevel = newVolume)
                } else {
                    selectedSound
                }
            }
            currentState.copy(selectedMixSounds = updatedSounds)
        }
    }

    /**
     * Save mix ke database
     */
    fun saveMix() = viewModelScope.launch {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        val state = uiState.value

        // Validasi
        if (state.mixNameInput.isBlank()) {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    errorMessage = "Mix name cannot be empty."
                )
            }
            return@launch
        }

        // REQ-4.3: max 30 karakter
        if (state.mixNameInput.length > 30) {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    errorMessage = "Mix name maksimal 30 karakter."
                )
            }
            return@launch
        }

        if (state.selectedMixSounds.isEmpty()) {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    errorMessage = "Please select at least one sound."
                )
            }
            return@launch
        }

        val updatedMix = Mix(
            mixId = state.originalMixId,
            userId = state.userId,
            mixName = state.mixNameInput,
            creationDate = state.creationDate,
            lastModified = System.currentTimeMillis()  // Update lastModified
        )

        val updatedMixSounds = state.selectedMixSounds.map { selectedSound ->
            MixSound(
                mixId = state.originalMixId,
                soundId = selectedSound.soundId,
                volumeLevel = selectedSound.volumeLevel
            )
        }

        try {
            mixRepository.updateMix(updatedMix, updatedMixSounds)
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    errorMessage = "Failed to save mix: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}