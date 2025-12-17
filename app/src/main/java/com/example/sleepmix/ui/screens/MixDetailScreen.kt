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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepmix.repositori.AplikasiSleepMix
import com.example.sleepmix.room.MixSound
import com.example.sleepmix.viewmodel.MixDetailViewModel
import com.example.sleepmix.viewmodel.provider.MixDetailViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * PAGE7: Mix Detail Screen
 * FIXED: Volume sliders are now READ-ONLY (locked)
 * Volume can only be changed via PAGE9 (EditVolumeScreen)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixDetailScreen(
    mixId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: MixDetailViewModel = viewModel(
        factory = MixDetailViewModelFactory(AplikasiSleepMix.container.mixRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // CRITICAL FIX: Load sound details
    val soundRepository = AplikasiSleepMix.container.soundRepository
    var soundMap by remember { mutableStateOf<Map<Int, com.example.sleepmix.room.Sound>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val allSounds = soundRepository.getAllSounds()
        soundMap = allSounds.associateBy { it.soundId }
    }

    DisposableEffect(Unit) {
        viewModel.loadMix(mixId)
        viewModel.bindService(context)
        onDispose {
            viewModel.unbindService(context)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Mix?") },
            text = { Text("Are u sure?") },  // Per SRS
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            uiState.mixWithSounds?.mix?.let { mix ->
                                AplikasiSleepMix.container.mixRepository.deleteMix(mix)
                            }
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Yes", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.mixWithSounds?.mix?.mixName ?: "Mix Detail",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
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
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.mixWithSounds == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Mix not found")
                }
            }

            else -> {
                val mixWithSounds = uiState.mixWithSounds!!
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) {
                    // Play/Pause Card
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            FloatingActionButton(
                                onClick = { viewModel.togglePlayPause() },
                                modifier = Modifier.size(80.dp),
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = mixWithSounds.mix.mixName, style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(8.dp))

                            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            val dateString = dateFormat.format(Date(mixWithSounds.mix.creationDate))
                            Text(
                                text = "${mixWithSounds.sounds.size} sounds • Created $dateString",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Text(
                        text = "Sounds",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mixWithSounds.sounds) { mixSound ->
                            val sound = soundMap[mixSound.soundId]

                            // FIXED: Sliders are now READ-ONLY (locked)
                            MixSoundCardLocked(
                                mixSound = mixSound,
                                soundName = sound?.name ?: "Unknown",
                                soundIcon = sound?.iconRes ?: android.R.drawable.ic_media_play
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * FIXED: Read-only sound card with LOCKED slider
 * Volume cannot be changed here - only via EditVolumeScreen (PAGE9)
 */
@Composable
fun MixSoundCardLocked(
    mixSound: MixSound,
    soundName: String,
    soundIcon: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = soundIcon),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = soundName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Volume: ${(mixSound.volumeLevel * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Lock icon to indicate read-only
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FIXED: Read-only slider (disabled, not interactive)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (mixSound.volumeLevel == 0f)
                        Icons.Default.VolumeOff
                    else
                        Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(8.dp))

                // LOCKED SLIDER: enabled = false (read-only)
                Slider(
                    value = mixSound.volumeLevel,
                    onValueChange = { /* DO NOTHING - LOCKED */ },
                    modifier = Modifier.weight(1f),
                    valueRange = 0f..1f,
                    enabled = false  // CRITICAL: Disabled = locked
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(mixSound.volumeLevel * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(48.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info text
            Text(
                text = "💡 To adjust volume, use Edit Mix → Click sound",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}