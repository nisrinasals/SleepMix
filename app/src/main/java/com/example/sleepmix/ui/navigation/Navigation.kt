package com.example.sleepmix.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sleepmix.repositori.AplikasiSleepMix
import com.example.sleepmix.room.Sound
import com.example.sleepmix.ui.screens.*

/**
 * Navigation Routes - Sesuai SRS Section 4.1
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")

    // PAGE2: Home Screen (Sound Library) - Sesuai SRS
    object Home : Screen("home/{userId}") {
        fun createRoute(userId: Int) = "home/$userId"
    }

    // PAGE5: Sound Detail Screen
    object SoundDetail : Screen("sound_detail/{soundId}") {
        fun createRoute(soundId: Int) = "sound_detail/$soundId"
    }

    // PAGE3 & PAGE4: MyMix List (includes empty state)
    object MyMixList : Screen("my_mix/{userId}") {
        fun createRoute(userId: Int) = "my_mix/$userId"
    }

    // PAGE6: Create Mix Screen
    object CreateMix : Screen("create_mix/{userId}") {
        fun createRoute(userId: Int) = "create_mix/$userId"
    }

    // PAGE7: Mix Detail Screen
    object MixDetail : Screen("mix_detail/{mixId}/{userId}") {
        fun createRoute(mixId: Int, userId: Int) = "mix_detail/$mixId/$userId"
    }

    // PAGE8: Edit Mix Screen
    object EditMix : Screen("edit_mix/{mixId}/{userId}") {
        fun createRoute(mixId: Int, userId: Int) = "edit_mix/$mixId/$userId"
    }

    // PAGE9: Edit Volume Screen
    object EditVolume : Screen("edit_volume/{mixId}/{soundId}") {
        fun createRoute(mixId: Int, soundId: Int) = "edit_volume/$mixId/$soundId"
    }

    // PAGE10: Select Sound Screen (NEW - Sesuai SRS)
    object SelectSound : Screen("select_sound/{userId}/{fromEdit}/{mixId}") {
        fun createRoute(userId: Int, fromEdit: Boolean, mixId: Int = 0) =
            "select_sound/$userId/$fromEdit/$mixId"
    }

    // PAGE11: Set Volume Screen (NEW - Sesuai SRS)
    object SetVolume : Screen("set_volume/{soundId}/{userId}/{fromEdit}/{mixId}") {
        fun createRoute(soundId: Int, userId: Int, fromEdit: Boolean, mixId: Int = 0) =
            "set_volume/$soundId/$userId/$fromEdit/$mixId"
    }
}

/**
 * Main Navigation Host
 * Sesuai SRS Activity Diagram flow
 */
@Composable
fun SleepMixNavigation(
    navController: NavHostController = rememberNavController()
) {
    // Temporary storage untuk selected sounds (dalam create/edit mix flow)
    val selectedSoundsMap = remember { mutableStateMapOf<Int, Float>() }  // soundId -> volume

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // PAGE1: LOGIN SCREEN
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { userId ->
                    navController.navigate(Screen.Home.createRoute(userId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // REGISTER SCREEN
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // PAGE2: HOME SCREEN (Sound Library) - Sesuai SRS Section 4.1 Screen 2
        composable(
            route = Screen.Home.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0

            HomeScreen(
                userId = userId,
                onNavigateToMyMix = {
                    navController.navigate(Screen.MyMixList.createRoute(userId))
                },
                onNavigateToSoundDetail = { soundId ->
                    navController.navigate(Screen.SoundDetail.createRoute(soundId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // PAGE5: SOUND DETAIL SCREEN
        composable(
            route = Screen.SoundDetail.route,
            arguments = listOf(navArgument("soundId") { type = NavType.IntType })
        ) { backStackEntry ->
            val soundId = backStackEntry.arguments?.getInt("soundId") ?: 0

            SoundDetailScreen(
                soundId = soundId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // PAGE3 & PAGE4: MY MIX LIST SCREEN
        composable(
            route = Screen.MyMixList.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0

            MyMixListScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMixDetail = { mixId ->
                    navController.navigate(Screen.MixDetail.createRoute(mixId, userId))
                },
                onNavigateToCreateMix = {
                    // Clear selected sounds before creating new mix
                    selectedSoundsMap.clear()
                    navController.navigate(Screen.CreateMix.createRoute(userId))
                }
            )
        }

        // PAGE6: CREATE MIX SCREEN
        composable(
            route = Screen.CreateMix.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0

            CreateMixScreen(
                userId = userId,
                onNavigateBack = {
                    selectedSoundsMap.clear()
                    navController.popBackStack()
                },
                onMixCreated = {
                    selectedSoundsMap.clear()
                    navController.navigate(Screen.MyMixList.createRoute(userId)) {
                        popUpTo(Screen.Home.createRoute(userId)) {
                            inclusive = false
                        }
                    }
                },
                // NEW: Navigate to PAGE10 untuk add sound
                onNavigateToSelectSound = {
                    navController.navigate(Screen.SelectSound.createRoute(userId, false, 0))
                }
            )
        }

        // PAGE7: MIX DETAIL SCREEN
        composable(
            route = Screen.MixDetail.route,
            arguments = listOf(
                navArgument("mixId") { type = NavType.IntType },
                navArgument("userId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val mixId = backStackEntry.arguments?.getInt("mixId") ?: 0
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0

            MixDetailScreen(
                mixId = mixId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(Screen.EditMix.createRoute(mixId, userId))
                }
            )
        }

        // PAGE8: EDIT MIX SCREEN
        composable(
            route = Screen.EditMix.route,
            arguments = listOf(
                navArgument("mixId") { type = NavType.IntType },
                navArgument("userId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val mixId = backStackEntry.arguments?.getInt("mixId") ?: 0
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0

            EditMixScreen(
                mixId = mixId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onNavigateToEditVolume = { soundId ->
                    navController.navigate(Screen.EditVolume.createRoute(mixId, soundId))
                },
                // NEW: Navigate to PAGE10 untuk add sound
                onNavigateToSelectSound = {
                    navController.navigate(Screen.SelectSound.createRoute(userId, true, mixId))
                }
            )
        }

        // PAGE9: EDIT VOLUME SCREEN
        composable(
            route = Screen.EditVolume.route,
            arguments = listOf(
                navArgument("mixId") { type = NavType.IntType },
                navArgument("soundId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val mixId = backStackEntry.arguments?.getInt("mixId") ?: 0
            val soundId = backStackEntry.arguments?.getInt("soundId") ?: 0

            EditVolumeScreen(
                mixId = mixId,
                soundId = soundId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSoundRemoved = {
                    navController.popBackStack()
                }
            )
        }

        // PAGE10: SELECT SOUND SCREEN (NEW - Sesuai SRS)
        composable(
            route = Screen.SelectSound.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("fromEdit") { type = NavType.BoolType },
                navArgument("mixId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val fromEdit = backStackEntry.arguments?.getBoolean("fromEdit") ?: false
            val mixId = backStackEntry.arguments?.getInt("mixId") ?: 0

            // Get excluded sound IDs (already in mix)
            val excludedIds = selectedSoundsMap.keys.toList()

            SelectSoundScreen(
                excludedSoundIds = excludedIds,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSoundSelected = { sound ->
                    // Navigate to PAGE11 untuk set volume
                    navController.navigate(
                        Screen.SetVolume.createRoute(sound.soundId, userId, fromEdit, mixId)
                    )
                }
            )
        }

        // PAGE11: SET VOLUME SCREEN (NEW - Sesuai SRS)
        composable(
            route = Screen.SetVolume.route,
            arguments = listOf(
                navArgument("soundId") { type = NavType.IntType },
                navArgument("userId") { type = NavType.IntType },
                navArgument("fromEdit") { type = NavType.BoolType },
                navArgument("mixId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val soundId = backStackEntry.arguments?.getInt("soundId") ?: 0
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val fromEdit = backStackEntry.arguments?.getBoolean("fromEdit") ?: false
            val mixId = backStackEntry.arguments?.getInt("mixId") ?: 0

            SetVolumeScreenWrapper(
                soundId = soundId,
                userId = userId,
                fromEdit = fromEdit,
                mixId = mixId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddToMix = { sid, volume ->
                    // Save to temporary map
                    selectedSoundsMap[sid] = volume

                    // Navigate back sesuai Activity Diagram:
                    // BackToEdit? Yes -> EditMode, No -> CreateMix
                    if (fromEdit) {
                        // Go back to Edit Mix (skip Select Sound)
                        navController.popBackStack(Screen.EditMix.createRoute(mixId, userId), false)
                    } else {
                        // Go back to Create Mix (skip Select Sound)
                        navController.popBackStack(Screen.CreateMix.createRoute(userId), false)
                    }
                }
            )
        }
    }
}

/**
 * Wrapper untuk SetVolumeScreen yang fetch sound dari repository
 */
@Composable
fun SetVolumeScreenWrapper(
    soundId: Int,
    userId: Int,
    fromEdit: Boolean,
    mixId: Int,
    onNavigateBack: () -> Unit,
    onAddToMix: (Int, Float) -> Unit
) {
    // Fetch sound data
    val soundRepository = AplikasiSleepMix.container.soundRepository
    var sound by remember { mutableStateOf<Sound?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(soundId) {
        isLoading = true
        sound = soundRepository.getSoundById(soundId)
        isLoading = false
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        sound != null -> {
            SetVolumeScreen(
                sound = sound!!,
                onNavigateBack = onNavigateBack,
                onAddToMix = onAddToMix
            )
        }
        else -> {
            // Sound not found - go back
            LaunchedEffect(Unit) {
                onNavigateBack()
            }
        }
    }
}