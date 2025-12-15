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

data class SelectedMixSound(
    val soundId: Int,
    val name: String,
    val iconRes: Int,
    var volumeLevel: Float = 0.5f // Default volume 50%
)

data class CreateMixUiState(
    // Daftar semua suara bawaan yang tersedia untuk dipilih
    val availableSounds: List<Sound> = emptyList(),
    // Daftar suara yang sudah dipilih oleh pengguna, termasuk volume setting-nya
    val selectedMixSounds: List<SelectedMixSound> = emptyList(),
    val mixNameInput: String = "",
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class CreateMixViewModel(
    private val mixRepository: MixRepository,
    private val soundRepository: SoundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateMixUiState())
    val uiState: StateFlow<CreateMixUiState> = _uiState.asStateFlow()

    init {
        // Load semua Sound yang tersedia saat ViewModel dibuat
        loadAvailableSounds()
    }

    private fun loadAvailableSounds() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val sounds = soundRepository.getAllSounds() // Dari SoundRepository
                _uiState.update {
                    it.copy(
                        availableSounds = sounds,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat suara: ${e.message}") }
            }
        }
    }

    fun updateMixName(name: String) {
        _uiState.update { it.copy(mixNameInput = name) }
    }

    // Aksi: Menambah/Menghapus Sound ke/dari Mix
    fun toggleSoundSelection(sound: Sound) {
        _uiState.update { currentState ->
            val isSelected = currentState.selectedMixSounds.any { it.soundId == sound.soundId }

            val newSelectedSounds = if (isSelected) {
                // Hapus Sound
                currentState.selectedMixSounds.filter { it.soundId != sound.soundId }
            } else {
                // Tambah Sound dengan default volume 0.5f (50%)
                currentState.selectedMixSounds + SelectedMixSound(
                    soundId = sound.soundId,
                    name = sound.name,
                    iconRes = sound.iconRes,
                    volumeLevel = 0.5f // Default volume
                )
            }
            return@update currentState.copy(selectedMixSounds = newSelectedSounds)
        }
    }

    // Aksi: Mengubah Volume Sound yang sudah dipilih
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

    // Aksi Utama: Menyimpan Mix ke Database (sesuai Activity Diagram)
    fun saveMix(currentUserId: Int) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val state = uiState.value
        if (state.mixNameInput.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Nama Mix tidak boleh kosong.") }
            return@launch
        }
        if (state.selectedMixSounds.isEmpty()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Pilih minimal satu Sound.") }
            return@launch
        }

        // 1. Buat Entitas Mix
        val newMix = Mix(
            userId = currentUserId,
            mixName = state.mixNameInput,
            creationDate = Date().time // Gunakan Long timestamp
        )

        // 2. Buat daftar Entitas MixSound
        val newMixSounds = state.selectedMixSounds.map { selectedSound ->
            MixSound(
                mixId = 0, // mixId akan diisi oleh Repository saat transaksi (insert)
                soundId = selectedSound.soundId,
                volumeLevel = selectedSound.volumeLevel
            )
        }

        try {
            // 3. Panggil operasi transaksi di MixRepository
            mixRepository.createMix(newMix, newMixSounds)

            // 4. Success
            _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal menyimpan Mix: ${e.message}") }
        }
    }
}