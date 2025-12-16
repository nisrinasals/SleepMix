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
import com.example.sleepmix.room.Sound
import com.example.sleepmix.ui.components.AvailableSoundItem
import com.example.sleepmix.viewmodel.EditMixViewModel
import com.example.sleepmix.viewmodel.provider.EditMixViewModelFactory
import com.example.sleepmix.viewmodel.SelectedMixSound

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMixScreen(
    mixId: Int,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    onNavigateToEditVolume: (Int) -> Unit,  // NEW: Navigate to PAGE9
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
                                SelectedSoundItemClickable(
                                    selectedSound = selectedSound,
                                    onClick = {
                                        // NEW: Navigate to PAGE9 (EditVolume)
                                        onNavigateToEditVolume(selectedSound.soundId)
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

@Composable
fun SelectedSoundItemClickable(
    selectedSound: SelectedMixSound,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),  // NEW: Clickable to navigate to PAGE9
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = selectedSound.iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.width(16.dp))

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
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
