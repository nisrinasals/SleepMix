package com.example.sleepmix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// These are placeholder screens - implement based on your requirements

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseSoundScreen(
    userId: Int,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse Sounds") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Browse Sound Screen - TODO: Implement sound browsing")
            Text("Show grid of available sounds with play button")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMixListScreen(
    userId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToMixDetail: (Int) -> Unit,
    onNavigateToCreateMix: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Mixes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateMix) {
                Icon(Icons.Default.Add, "Create Mix")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("My Mix List Screen - TODO: Implement mix list")
            Text("Show list of user's saved mixes")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMixScreen(
    userId: Int,
    onNavigateBack: () -> Unit,
    onMixCreated: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Mix") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create Mix Screen - TODO: Implement mix creation")
            Text("Allow user to select sounds and set volumes")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixDetailScreen(
    mixId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mix Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Mix Detail Screen - TODO: Implement mix playback")
            Text("Show mix details, play/pause, volume controls")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToEdit) {
                Text("Edit Mix")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMixScreen(
    mixId: Int,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Mix") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Edit Mix Screen - TODO: Implement mix editing")
            Text("Allow user to modify sound selection and volumes")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onSaveSuccess) {
                Text("Save Changes")
            }
        }
    }
}