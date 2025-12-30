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
import com.example.sleepmix.room.Sound
import android.media.MediaPlayer
import android.util.Log

/**
 * PAGE11: Set Initial Volume Screen
 * Sesuai Activity Diagram flow:
 * PAGE10 (Select Sound) → PAGE11 (Set Initial Volume) → Add to Mix
 *
 * Fitur:
 * - Sound icon dan name
 * - Play button untuk preview
 * - Volume slider
 * - "Add to mix" button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetVolumeScreen(
    sound: Sound,
    onNavigateBack: () -> Unit,
    onAddToMix: (soundId: Int, volumeLevel: Float) -> Unit
) {
    val context = LocalContext.current
    var volumeLevel by remember { mutableStateOf(0.5f) }  // Default 50%
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyMix") },  // Sesuai SRS
                navigationIcon = {
                    IconButton(onClick = {
                        // Stop preview before navigating back
                        mediaPlayer?.apply {
                            if (isPlaying) stop()
                            release()
                        }
                        mediaPlayer = null
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

            // Preview Play/Pause Button
            FloatingActionButton(
                onClick = {
                    try {
                        if (isPlaying) {
                            // Stop preview
                            mediaPlayer?.pause()
                            isPlaying = false
                        } else {
                            // Start preview
                            if (mediaPlayer == null) {
                                val resourceId = sound.filePath.substringAfterLast("/").toIntOrNull()
                                if (resourceId != null && resourceId != 0) {
                                    mediaPlayer = MediaPlayer.create(context, resourceId)?.apply {
                                        isLooping = true
                                        setVolume(volumeLevel, volumeLevel)
                                    }
                                }
                            }
                            mediaPlayer?.start()
                            isPlaying = true
                        }
                    } catch (e: Exception) {
                        Log.e("SetVolumeScreen", "Error playing preview", e)
                    }
                },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

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
                            imageVector = if (volumeLevel == 0f)
                                Icons.Default.VolumeOff
                            else
                                Icons.Default.VolumeUp,
                            contentDescription = "Volume"
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Slider(
                            value = volumeLevel,
                            onValueChange = { newVolume ->
                                volumeLevel = newVolume
                                // Update preview volume in real-time
                                mediaPlayer?.setVolume(newVolume, newVolume)
                            },
                            modifier = Modifier.weight(1f),
                            valueRange = 0f..1f
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "${(volumeLevel * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.width(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Add to Mix Button - Sesuai Activity Diagram
            Button(
                onClick = {
                    // Stop preview before adding
                    mediaPlayer?.apply {
                        if (isPlaying) stop()
                        release()
                    }
                    mediaPlayer = null
                    isPlaying = false

                    // Add sound to mix dengan volume yang dipilih
                    onAddToMix(sound.soundId, volumeLevel)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add to Mix", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
