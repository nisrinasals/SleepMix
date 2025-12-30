package com.example.sleepmix.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.sleepmix.viewmodel.HomeViewModel
import com.example.sleepmix.viewmodel.provider.HomeViewModelFactory

/**
 * PAGE2: Home Screen (Sound Library)
 * Sesuai SRS Section 4.1 Screen 2:
 * - Header: App name "SleepMix" (left), "My Mix" button (right)
 * - Search Bar
 * - Content Area: Sound cards dengan sound icon dan sound name
 * - Button: Logout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: Int,
    onNavigateToMyMix: () -> Unit,
    onNavigateToSoundDetail: (Int) -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(AplikasiSleepMix.container.soundRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Logout confirmation dialog - Sesuai SRS Section 3.5.2 Sequence 4
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure?") },  // Sesuai SRS Section 2.6
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SleepMix") },
                actions = {
                    // "My Mix" button - Sesuai SRS
                    Button(
                        onClick = onNavigateToMyMix,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("My Mix")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            // Logout button di bottom - Sesuai SRS
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar - Sesuai SRS
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search sounds...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, "Clear")
                        }
                    }
                },
                singleLine = true
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
                    // Filter sounds berdasarkan search query
                    val filteredSounds = if (searchQuery.isEmpty()) {
                        uiState.sounds
                    } else {
                        uiState.sounds.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (filteredSounds.isEmpty()) {
                        // Empty state - Sesuai SRS Section 2.6: "No sounds found"
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
                                    text = "No sounds found",  // Sesuai SRS Section 2.6
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    } else {
                        // Sound Grid - Sesuai SRS: Sound cards dengan sound icon dan sound name
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = filteredSounds,
                                key = { it.soundId }
                            ) { sound ->
                                SoundCardHome(
                                    sound = sound,
                                    onClick = { onNavigateToSoundDetail(sound.soundId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sound Card untuk Home Screen
 * Sesuai SRS: Sound cards dengan sound icon dan sound name
 */
@Composable
fun SoundCardHome(
    sound: Sound,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Sound Icon - Sesuai SRS
                Icon(
                    painter = painterResource(id = sound.iconRes),
                    contentDescription = sound.name,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sound Name - Sesuai SRS
                Text(
                    text = sound.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
