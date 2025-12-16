package com.example.sleepmix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepmix.repositori.AplikasiSleepMix
import com.example.sleepmix.viewmodel.EditVolumeViewModel
import com.example.sleepmix.viewmodel.provider.EditVolumeViewModelFactory
import kotlinx.coroutines.launch

/**
 * PAGE9: Edit Volume Screen
 * Allows user to:
 * - Adjust volume of individual sound in mix
 * - Preview sound with volume
 * - Remove sound from mix
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVolumeScreen(
    mixId: Int,
    soundId: Int,
    onNavigateBack: () -> Unit,
    onSoundRemoved: () -> Unit,
    viewModel: EditVolumeViewModel = viewModel(
        factory = EditVolumeViewModelFactory(
            mixRepository = AplikasiSleepMix.container.mixRepository,
            soundRepository = AplikasiSleepMix.container.soundRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showRemoveDialog by remember { mutableStateOf(false) }

    // Load data on composition
    LaunchedEffect(mixId, soundId) {
        viewModel.loadMixSound(mixId, soundId)
    }

    // Handle update success
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            onNavigateBack()
        }
    }

    // Handle remove success
    LaunchedEffect(uiState.removeSuccess) {
        if (uiState.removeSuccess) {
            onSoundRemoved()
        }
    }

    // Remove confirmation dialog
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Hapus Sound") },
            text = { Text("Are u sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveDialog = false
                        viewModel.removeSound()
                    }
                ) {
                    Text("Yes", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyMix") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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

            uiState.sound == null || uiState.mixSound == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sound not found")
                }
            }

            else -> {
                val sound = uiState.sound!!
                val mixSound = uiState.mixSound!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Sound Icon
                    Icon(
                        painter = painterResource(id = sound.iconRes),
                        contentDescription = sound.name,
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sound Name
                    Text(
                        text = sound.name,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Play/Pause Button
                    FloatingActionButton(
                        onClick = {
                            if (uiState.isPlaying) {
                                viewModel.stopPreview()
                            } else {
                                viewModel.playPreview(sound, context)
                            }
                        },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Volume Section
                    Text(
                        text = "Volume",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.volumeLevel == 0f)
                                Icons.Default.VolumeOff
                            else
                                Icons.Default.VolumeUp,
                            contentDescription = "Volume"
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Slider(
                            value = uiState.volumeLevel,
                            onValueChange = { newVolume ->
                                viewModel.updateVolumeLevel(newVolume)
                                // Update preview volume if playing
                                if (uiState.isPlaying) {
                                    viewModel.updatePreviewVolume(newVolume)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            valueRange = 0f..1f
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "${(uiState.volumeLevel * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.width(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Error Message
                    if (uiState.errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Update Button
                    Button(
                        onClick = { viewModel.updateSound() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Update")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Remove Button
                    OutlinedButton(
                        onClick = { showRemoveDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = !uiState.isSaving
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hapus dari mix")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPreview()
        }
    }
}