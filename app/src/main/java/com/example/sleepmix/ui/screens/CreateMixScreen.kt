package com.example.sleepmix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepmix.repositori.AplikasiSleepMix
import com.example.sleepmix.viewmodel.CreateMixViewModel
import com.example.sleepmix.viewmodel.SelectedMixSound
import com.example.sleepmix.viewmodel.provider.CreateMixViewModelFactory

/**
 * PAGE6: Create Mix Screen
 * Sesuai SRS Section 4.1 Screen 6:
 * - Header: Text "MyMix" (left), Back button ←
 * - Content Area: Mix name input, Sounds list (initially empty)
 * - Buttons: "Save" button
 * - FAB "+" untuk menambah mix (navigate ke PAGE10)
 *
 * Flow sesuai Activity Diagram:
 * CreateMix → EnterName → AddSounds (click + → PAGE10) → Save
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMixScreen(
    userId: Int,
    onNavigateBack: () -> Unit,
    onMixCreated: () -> Unit,
    onNavigateToSelectSound: () -> Unit,  // NEW: Navigate ke PAGE10
    viewModel: CreateMixViewModel = viewModel(
        factory = CreateMixViewModelFactory(
            mixRepository = AplikasiSleepMix.container.mixRepository,
            soundRepository = AplikasiSleepMix.container.soundRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onMixCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyMix") },  // Sesuai SRS
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        // FAB "+" untuk menambah sound - Sesuai SRS
        floatingActionButton = {
            // REQ-2.4: Prevent duplicate & REQ-2.1: Max 5 sounds
            if (uiState.selectedMixSounds.size < 5) {
                FloatingActionButton(
                    onClick = onNavigateToSelectSound  // Navigate ke PAGE10
                ) {
                    Icon(Icons.Default.Add, "Add Sound")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mix Name Input - Sesuai SRS
            // REQ-4.3: maksimal 30 karakter
            OutlinedTextField(
                value = uiState.mixNameInput,
                onValueChange = { newValue ->
                    // Validasi max 30 karakter sesuai SRS REQ-4.3
                    if (newValue.length <= 30) {
                        viewModel.updateMixName(newValue)
                    }
                },
                label = { Text("Mix Name") },
                placeholder = { Text("Enter mix name (max 30 chars)") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                supportingText = {
                    Text("${uiState.mixNameInput.length}/30")
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !uiState.isLoading
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

            // Sounds List Section - Sesuai SRS
            if (uiState.selectedMixSounds.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tap + to add sounds",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Sounds list - Sesuai SRS: display added sounds with Icon + Name
                Text(
                    text = "Sounds (${uiState.selectedMixSounds.size}/5)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.selectedMixSounds) { selectedSound ->
                        CreateMixSoundItem(
                            selectedSound = selectedSound,
                            onRemove = { viewModel.removeSound(selectedSound.soundId) },
                            onVolumeChange = { newVolume ->
                                viewModel.updateSelectedSoundVolume(selectedSound.soundId, newVolume)
                            }
                        )
                    }
                }
            }

            // Save Button - Sesuai SRS
            Button(
                onClick = { viewModel.saveMix(userId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                enabled = !uiState.isLoading &&
                        uiState.mixNameInput.isNotBlank() &&
                        uiState.selectedMixSounds.isNotEmpty()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/**
 * Sound Item untuk Create Mix Screen
 * Sesuai SRS: display added sounds with Icon + Name (horizontal layout)
 */
@Composable
fun CreateMixSoundItem(
    selectedSound: SelectedMixSound,
    onRemove: () -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sound Icon - Sesuai SRS
                Icon(
                    painter = painterResource(id = selectedSound.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Sound Name - Sesuai SRS
                Text(
                    text = selectedSound.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Remove button
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Volume Slider - REQ-3.1: independent volume control 0-100%
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (selectedSound.volumeLevel == 0f)
                        Icons.Default.VolumeOff
                    else
                        Icons.Default.VolumeUp,
                    contentDescription = "Volume"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Slider(
                    value = selectedSound.volumeLevel,
                    onValueChange = onVolumeChange,
                    modifier = Modifier.weight(1f),
                    valueRange = 0f..1f
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${(selectedSound.volumeLevel * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(48.dp)
                )
            }
        }
    }
}
