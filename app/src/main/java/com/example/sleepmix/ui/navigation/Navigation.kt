package com.example.sleepmix.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sleepmix.ui.screens.*

/**
 * Sealed class untuk mendefinisikan semua route navigasi
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home/{userId}") {
        fun createRoute(userId: Int) = "home/$userId"
    }
    object BrowseSound : Screen("browse/{userId}") {
        fun createRoute(userId: Int) = "browse/$userId"
    }
    object MyMixList : Screen("my_mix/{userId}") {
        fun createRoute(userId: Int) = "my_mix/$userId"
    }
    object CreateMix : Screen("create_mix/{userId}") {
        fun createRoute(userId: Int) = "create_mix/$userId"
    }
    object MixDetail : Screen("mix_detail/{mixId}") {
        fun createRoute(mixId: Int) = "mix_detail/$mixId"
    }
    object EditMix : Screen("edit_mix/{mixId}") {
        fun createRoute(mixId: Int) = "edit_mix/$mixId"
    }
}

/**
 * Main Navigation Composable
 */
@Composable
fun SleepMixNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { userId ->
                    navController.navigate(Screen.Home.createRoute(userId)) {
                        // Clear back stack sampai login
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Register Screen
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

        // Home Screen
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

        // Browse Sound Screen
        composable(
            route = Screen.BrowseSound.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            BrowseSoundScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // My Mix List Screen
        composable(
            route = Screen.MyMixList.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            MyMixListScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMixDetail = { mixId ->
                    navController.navigate(Screen.MixDetail.createRoute(mixId))
                },
                onNavigateToCreateMix = {
                    navController.navigate(Screen.CreateMix.createRoute(userId))
                }
            )
        }

        // Create Mix Screen
        composable(
            route = Screen.CreateMix.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            CreateMixScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
                onMixCreated = {
                    navController.popBackStack()
                }
            )
        }

        // Mix Detail Screen
        composable(
            route = Screen.MixDetail.route,
            arguments = listOf(navArgument("mixId") { type = NavType.IntType })
        ) { backStackEntry ->
            val mixId = backStackEntry.arguments?.getInt("mixId") ?: 0
            MixDetailScreen(
                mixId = mixId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(Screen.EditMix.createRoute(mixId))
                }
            )
        }

        // Edit Mix Screen
        composable(
            route = Screen.EditMix.route,
            arguments = listOf(navArgument("mixId") { type = NavType.IntType })
        ) { backStackEntry ->
            val mixId = backStackEntry.arguments?.getInt("mixId") ?: 0
            EditMixScreen(
                mixId = mixId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
    }
}