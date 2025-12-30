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
import com.example.sleepmix.viewmodel.SelectSoundViewModel
import com.example.sleepmix.viewmodel.provider.SelectSoundViewModelFactory

/**
 * PAGE10: Select Sound Screen
 * Sesuai SRS Section 4.1 Screen 10:
 * - Header: Text "MyMix" (left), Back button ← (navigate to previous page)
 * - Content Area: Sound cards dengan sound icon dan sound name
 *
 * Flow sesuai Activity Diagram:
 * CreateMix/EditMix → PAGE10 → Pick Sound → PAGE11 (Set Volume)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSoundScreen(
    excludedSoundIds: List<Int>,  // Sounds yang sudah dipilih (untuk prevent duplicate)
    onNavigateBack: () -> Unit,
    onSoundSelected: (Sound) -> Unit,  // Navigate ke PAGE11 dengan sound terpilih
    viewModel: SelectSoundViewModel = viewModel(
        factory = SelectSoundViewModelFactory(AplikasiSleepMix.container.soundRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // Filter out sounds yang sudah ada di mix
    val availableSounds = uiState.sounds.filter { sound ->
        sound.soundId !in excludedSoundIds
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

            availableSounds.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "All sounds have been added to mix",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            else -> {
                // Sound cards list - Sesuai SRS: sound icon dan sound name
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(availableSounds, key = { it.soundId }) { sound ->
                        SelectableSoundCard(
                            sound = sound,
                            onClick = { onSoundSelected(sound) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Selectable Sound Card untuk PAGE10
 * Sesuai SRS: Sound cards dengan sound icon dan sound name
 */
@Composable
fun SelectableSoundCard(
    sound: Sound,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                painter = painterResource(id = sound.iconRes),
                contentDescription = sound.name,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Sound Name - Sesuai SRS
            Text(
                text = sound.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Arrow indicator
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
