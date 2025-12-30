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
import java.util.Date

/**
 * Data class untuk sound yang dipilih dalam mix
 */
data class SelectedMixSound(
    val soundId: Int,
    val name: String,
    val iconRes: Int,
    var volumeLevel: Float = 0.5f // Default volume 50%
)

/**
 * UI State untuk Create Mix Screen
 */
data class CreateMixUiState(
    val availableSounds: List<Sound> = emptyList(),
    val selectedMixSounds: List<SelectedMixSound> = emptyList(),
    val mixNameInput: String = "",
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk PAGE6: Create Mix Screen
 * Sesuai SRS Section 3.4 MyMix Management
 */
class CreateMixViewModel(
    private val mixRepository: MixRepository,
    private val soundRepository: SoundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateMixUiState())
    val uiState: StateFlow<CreateMixUiState> = _uiState.asStateFlow()

    init {
        loadAvailableSounds()
    }

    private fun loadAvailableSounds() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val sounds = soundRepository.getAllSounds()
                _uiState.update {
                    it.copy(
                        availableSounds = sounds,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Gagal memuat suara: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Update mix name
     * REQ-4.3: maksimal 30 karakter (validasi di UI)
     */
    fun updateMixName(name: String) {
        _uiState.update { it.copy(mixNameInput = name, errorMessage = null) }
    }

    /**
     * Toggle sound selection (untuk backward compatibility)
     * REQ-2.4: Sistem harus mencegah User menambahkan suara yang sama lebih dari satu kali
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
     * Remove sound dari selection
     */
    fun removeSound(soundId: Int) {
        _uiState.update { currentState ->
            val newSelectedSounds = currentState.selectedMixSounds.filter {
                it.soundId != soundId
            }
            currentState.copy(selectedMixSounds = newSelectedSounds)
        }
    }

    /**
     * Update volume untuk sound yang dipilih
     * REQ-3.1: independent volume control 0-100%
     * REQ-3.3: Perubahan volume harus applied secara real-time
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
     * Sesuai Activity Diagram: ValidateMix → INSERT INTO MIX → INSERT INTO MIX_SOUND
     */
    fun saveMix(currentUserId: Int) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val state = uiState.value

        // Validasi - sesuai Activity Diagram ValidateMix
        if (state.mixNameInput.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Nama Mix tidak boleh kosong."
                )
            }
            return@launch
        }

        // REQ-4.3: max 30 karakter
        if (state.mixNameInput.length > 30) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Nama Mix maksimal 30 karakter."
                )
            }
            return@launch
        }

        if (state.selectedMixSounds.isEmpty()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Pilih minimal satu Sound."
                )
            }
            return@launch
        }

        // REQ-2.1: minimal 2, maksimal 5
        if (state.selectedMixSounds.size < 2) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Pilih minimal 2 Sound untuk mix."
                )
            }
            return@launch
        }

        val currentTime = Date().time

        // INSERT INTO MIX - sesuai Activity Diagram
        val newMix = Mix(
            userId = currentUserId,
            mixName = state.mixNameInput,
            creationDate = currentTime,
            lastModified = currentTime  // NEW: sesuai schema SRS
        )

        // INSERT INTO MIX_SOUND - sesuai Activity Diagram
        val newMixSounds = state.selectedMixSounds.map { selectedSound ->
            MixSound(
                mixId = 0,  // akan diisi oleh Repository
                soundId = selectedSound.soundId,
                volumeLevel = selectedSound.volumeLevel
            )
        }

        try {
            mixRepository.createMix(newMix, newMixSounds)
            _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Gagal menyimpan Mix: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
