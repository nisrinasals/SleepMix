package com.example.sleepmix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepmix.repositori.AplikasiSleepMix
import com.example.sleepmix.room.Sound
import com.example.sleepmix.viewmodel.EditMixViewModel
import com.example.sleepmix.viewmodel.provider.EditMixViewModelFactory
import com.example.sleepmix.viewmodel.SelectedMixSound

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMixScreen(
    mixId: Int,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: EditMixViewModel = viewModel(
        factory = EditMixViewModelFactory(
            mixRepository = AplikasiSleepMix.container.mixRepository,
            soundRepository = AplikasiSleepMix.container.soundRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Load mix on composition
    LaunchedEffect(mixId) {
        viewModel.loadMix(mixId)
    }

    // Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Mix") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveMix() },
                        enabled = !uiState.isLoading &&
                                uiState.mixNameInput.isNotBlank() &&
                                uiState.selectedMixSounds.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Mix Name Input
                    OutlinedTextField(
                        value = uiState.mixNameInput,
                        onValueChange = { viewModel.updateMixName(it) },
                        label = { Text("Mix Name") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        enabled = !uiState.isSaving
                    )

                    // Error Message
                    if (uiState.errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Selected Sounds Section
                    if (uiState.selectedMixSounds.isNotEmpty()) {
                        Text(
                            text = "Selected Sounds (${uiState.selectedMixSounds.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.selectedMixSounds) { selectedSound ->
                                SelectedSoundItem(
                                    selectedSound = selectedSound,
                                    onVolumeChange = { newVolume ->
                                        viewModel.updateSelectedSoundVolume(
                                            selectedSound.soundId,
                                            newVolume
                                        )
                                    },
                                    onRemove = {
                                        val originalSound = uiState.availableSounds.find {
                                            it.soundId == selectedSound.soundId
                                        }
                                        originalSound?.let { viewModel.toggleSoundSelection(it) }
                                    }
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Available Sounds Section
                    Text(
                        text = "Available Sounds",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(if (uiState.selectedMixSounds.isEmpty()) 1f else 0.6f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.availableSounds) { sound ->
                            val isSelected = uiState.selectedMixSounds.any { it.soundId == sound.soundId }
                            AvailableSoundItem(
                                sound = sound,
                                isSelected = isSelected,
                                onToggleSelection = { viewModel.toggleSoundSelection(sound) }
                            )
                        }
                    }

                    // Loading indicator
                    if (uiState.isSaving) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}