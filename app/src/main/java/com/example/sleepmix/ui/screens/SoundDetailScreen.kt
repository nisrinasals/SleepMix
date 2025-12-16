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
import com.example.sleepmix.viewmodel.SoundDetailViewModel
import com.example.sleepmix.viewmodel.provider.SoundDetailViewModelFactory

/**
 * PAGE5: Sound Detail Screen
 * Full screen preview for individual sound with volume control
 * Per SRS Section 4.1 Screen 5
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundDetailScreen(
    soundId: Int,
    onNavigateBack: () -> Unit,
    viewModel: SoundDetailViewModel = viewModel(
        factory = SoundDetailViewModelFactory(AplikasiSleepMix.container.soundRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Load sound on composition
    LaunchedEffect(soundId) {
        viewModel.loadSound(soundId)
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSound()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SleepMix") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopSound()
                        onNavigateBack()
                    }) {
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

            uiState.sound == null -> {
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

                    // Sound Icon (Large)
                    Icon(
                        painter = painterResource(id = sound.iconRes),
                        contentDescription = sound.name,
                        modifier = Modifier.size(160.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Sound Name
                    Text(
                        text = sound.name,
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Play/Pause Button (Large)
                    FloatingActionButton(
                        onClick = {
                            if (uiState.isPlaying) {
                                viewModel.pauseSound()
                            } else {
                                viewModel.playSound(sound, context)
                            }
                        },
                        modifier = Modifier.size(80.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = if (uiState.isPlaying)
                                Icons.Default.Pause
                            else
                                Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Volume Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Volume",
                                style = MaterialTheme.typography.titleMedium
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
                                    contentDescription = "Volume",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Slider(
                                    value = uiState.volumeLevel,
                                    onValueChange = { newVolume ->
                                        viewModel.updateVolume(newVolume)
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
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error Message
                    if (uiState.errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}