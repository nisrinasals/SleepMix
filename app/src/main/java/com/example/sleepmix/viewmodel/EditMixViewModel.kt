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

    fun updateMixName(name: String) {
        _uiState.update { it.copy(mixNameInput = name) }
    }

    fun toggleSoundSelection(sound: Sound) {
        _uiState.update { currentState ->
            val isSelected = currentState.selectedMixSounds.any { it.soundId == sound.soundId }

            val newSelectedSounds = if (isSelected) {
                currentState.selectedMixSounds.filter { it.soundId != sound.soundId }
            } else {
                currentState.selectedMixSounds + SelectedMixSound(
                    soundId = sound.soundId,
                    name = sound.name,
                    iconRes = sound.iconRes,
                    volumeLevel = 0.5f
                )
            }
            currentState.copy(selectedMixSounds = newSelectedSounds)
        }
    }

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

    fun saveMix() = viewModelScope.launch {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        val state = uiState.value

        if (state.mixNameInput.isBlank()) {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    errorMessage = "Mix name cannot be empty."
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
            creationDate = state.creationDate
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
}