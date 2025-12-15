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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepmix.repositori.AplikasiSleepMix
import com.example.sleepmix.room.Sound
import com.example.sleepmix.viewmodel.CreateMixViewModel
import com.example.sleepmix.viewmodel.SelectedMixSound
import com.example.sleepmix.viewmodel.provider.CreateMixViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMixScreen(
    userId: Int,
    onNavigateBack: () -> Unit,
    onMixCreated: () -> Unit,
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
                title = { Text("Create Mix") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveMix(userId) },
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
                placeholder = { Text("e.g., Evening Relaxation") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
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
                                // Find the original sound and toggle selection
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
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun SelectedSoundItem(
    selectedSound: SelectedMixSound,
    onVolumeChange: (Float) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = selectedSound.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = selectedSound.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (selectedSound.volumeLevel == 0f)
                        Icons.Default.VolumeOff
                    else
                        Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableSoundItem(
    sound: Sound,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Card(
        onClick = onToggleSelection,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = sound.iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = sound.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}