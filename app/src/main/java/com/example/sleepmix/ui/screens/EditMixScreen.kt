package com.example.sleepmix.ui.screens

import androidx.compose.foundation.clickable
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
import com.example.sleepmix.viewmodel.EditMixViewModel
import com.example.sleepmix.viewmodel.provider.EditMixViewModelFactory
import com.example.sleepmix.viewmodel.SelectedMixSound

/**
 * PAGE8: Edit Mix Screen
 * Sesuai SRS Section 4.1 Screen 8:
 * - Header: Text "MyMix" (left), Back button ←
 * - Content Area: Mix name input, Sounds list (display added sounds with Icon + Name)
 * - FAB "+" untuk menambah mix (navigate ke PAGE10)
 *
 * Flow sesuai Activity Diagram:
 * MixDetail → EditMix → EditAction (Edit Name / Click Sound / Add Sound)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMixScreen(
    mixId: Int,
    pendingSoundId: Int? = null,      // Sound ID dari PAGE11
    pendingVolume: Float? = null,      // Volume dari PAGE11
    onPendingSoundConsumed: () -> Unit = {},  // Callback setelah sound dikonsumsi
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    onNavigateToEditVolume: (Int) -> Unit,  // Navigate ke PAGE9
    onNavigateToSelectSound: (List<Int>) -> Unit,  // Navigate ke PAGE10 dengan excluded IDs
    viewModel: EditMixViewModel = viewModel(
        factory = EditMixViewModelFactory(
            mixRepository = AplikasiSleepMix.container.mixRepository,
            soundRepository = AplikasiSleepMix.container.soundRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(mixId) {
        viewModel.loadMix(mixId)
    }

    // Handle pending sound dari PAGE11
    LaunchedEffect(pendingSoundId, pendingVolume) {
        if (pendingSoundId != null && pendingVolume != null) {
            viewModel.addSoundWithVolume(pendingSoundId, pendingVolume)
            onPendingSoundConsumed()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveSuccess()
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
            // REQ-2.1: Max 5 sounds
            if (uiState.selectedMixSounds.size < 5) {
                FloatingActionButton(
                    onClick = {
                        // Pass excluded sound IDs ke SelectSound screen
                        onNavigateToSelectSound(uiState.selectedMixSounds.map { it.soundId })
                    }
                ) {
                    Icon(Icons.Default.Add, "Add Sound")
                }
            }
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
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        supportingText = {
                            Text("${uiState.mixNameInput.length}/30")
                        },
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

                    // Sounds Section - Sesuai SRS
                    if (uiState.selectedMixSounds.isNotEmpty()) {
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
                                // Clickable sound card - navigate ke PAGE9
                                EditMixSoundItemClickable(
                                    selectedSound = selectedSound,
                                    onClick = {
                                        onNavigateToEditVolume(selectedSound.soundId)
                                    }
                                )
                            }
                        }
                    } else {
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
                                    text = "No sounds in mix. Tap + to add.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Save Button
                    Button(
                        onClick = { viewModel.saveMix() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        enabled = !uiState.isSaving &&
                                uiState.mixNameInput.isNotBlank() &&
                                uiState.selectedMixSounds.isNotEmpty()
                    ) {
                        if (uiState.isSaving) {
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
    }
}

/**
 * Clickable Sound Item untuk Edit Mix Screen
 * Sesuai SRS: display added sounds with Icon + Name (horizontal layout)
 * Click navigates to PAGE9 (Edit Volume)
 */
@Composable
fun EditMixSoundItemClickable(
    selectedSound: SelectedMixSound,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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

            // Sound Name dan Volume info - Sesuai SRS
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedSound.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(selectedSound.volumeLevel * 100).toInt()}% volume",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            // Arrow indicator untuk navigate ke PAGE9
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}