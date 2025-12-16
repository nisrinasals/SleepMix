package com.example.sleepmix.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sleepmix.ui.screens.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")

    object Home : Screen("home/{userId}") {
        fun createRoute(userId: Int) = "home/$userId"
    }

    object BrowseSound : Screen("browse/{userId}") {
        fun createRoute(userId: Int) = "browse/$userId"
    }

    // PAGE5: Sound Detail Screen
    object SoundDetail : Screen("sound_detail/{soundId}") {
        fun createRoute(soundId: Int) = "sound_detail/$soundId"
    }

    object MyMixList : Screen("my_mix/{userId}") {
        fun createRoute(userId: Int) = "my_mix/$userId"
    }

    object CreateMix : Screen("create_mix/{userId}") {
        fun createRoute(userId: Int) = "create_mix/$userId"
    }

    object MixDetail : Screen("mix_detail/{mixId}/{userId}") {
        fun createRoute(mixId: Int, userId: Int) = "mix_detail/$mixId/$userId"
    }

    object EditMix : Screen("edit_mix/{mixId}/{userId}") {
        fun createRoute(mixId: Int, userId: Int) = "edit_mix/$mixId/$userId"
    }

    // PAGE9: Edit Volume Screen
    object EditVolume : Screen("edit_volume/{mixId}/{soundId}") {
        fun createRoute(mixId: Int, soundId: Int) = "edit_volume/$mixId/$soundId"
    }
}

@Composable
fun SleepMixNavigation(
    navController: NavHostController = rememberNavController()
) {
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

        // PAGE2: HOME SCREEN
        composable(
            route = Screen.Home.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0

            HomeScreen(
                userId = userId,
                onNavigateToBrowse = {
                    navController.navigate(Screen.BrowseSound.createRoute(userId))
                },
                onNavigateToMyMix = {
                    navController.navigate(Screen.MyMixList.createRoute(userId))
                },
                onNavigateToCreateMix = {
                    navController.navigate(Screen.CreateMix.createRoute(userId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // BROWSE SOUND SCREEN
        composable(
            route = Screen.BrowseSound.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0

            BrowseSoundScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSoundDetail = { soundId ->
                    navController.navigate(Screen.SoundDetail.createRoute(soundId))
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

        // PAGE3: MY MIX LIST SCREEN
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
                    navController.popBackStack()
                },
                onMixCreated = {
                    navController.navigate(Screen.MyMixList.createRoute(userId)) {
                        popUpTo(Screen.Home.createRoute(userId)) {
                            inclusive = false
                        }
                    }
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
                    // Go back to EditMix after removing sound
                    navController.popBackStack()
                }
            )
        }
    }
}