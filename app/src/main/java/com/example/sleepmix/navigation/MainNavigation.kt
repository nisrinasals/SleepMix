package com.example.sleepmix.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sleepmix.repositori.AplikasiSleepMix
import com.example.sleepmix.viewmodel.LoginViewModelFactory
// Anda akan memerlukan import Factory lain di sini

/**
 * Mendefinisikan semua rute yang digunakan di aplikasi.
 */
object Routes {
    const val LOGIN = "login"
    const val REGISTRASI = "registrasi"
    const val MY_MIX = "my_mix" // Rute utama setelah login
    const val CREATE_MIX = "create_mix"
    const val MIX_DETAIL = "mix_detail/{mixId}"
    // Helper function untuk navigasi dengan argumen
    fun mixDetailRoute(mixId: Int) = "mix_detail/$mixId"
}


@Composable
fun MainNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // Ambil Factory dari AppContainer yang aman
    val loginFactory = LoginViewModelFactory(AplikasiSleepMix.container.userRepository)
    // val registrasiFactory = RegistrasiViewModelFactory(...)
    // val myMixFactory = MyMixViewModelFactory(...)

    // Asumsi: Aplikasi dimulai dari layar Login
    val startDestination = Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // --- 1. Rute LOGIN ---
        composable(Routes.LOGIN) {
            // Di sini Anda akan memanggil Composable LoginScreen/LoginRoute
            /*
            LoginRoute(
                 factory = loginFactory,
                 onLoginSuccess = {
                     navController.navigate(Routes.MY_MIX) {
                         // Hapus semua screen Auth dari Back Stack
                         popUpTo(Routes.LOGIN) { inclusive = true }
                     }
                 },
                 onNavigateToRegistration = { navController.navigate(Routes.REGISTRASI) }
             )
            */
        }

        // --- 2. Rute REGISTRASI ---
        composable(Routes.REGISTRASI) {
            // Di sini Anda akan memanggil Composable RegistrasiScreen/RegistrasiRoute
            /*
            RegistrasiRoute(
                 onRegistrationSuccess = {
                     // Kembali ke Login setelah registrasi berhasil
                     navController.popBackStack()
                 }
            )
            */
        }

        // --- 3. Rute MY MIX (Home) ---
        composable(Routes.MY_MIX) {
            // Di sini Anda akan memanggil Composable MyMixScreen
            /*
            MyMixRoute(
                 onNavigateToCreateMix = { navController.navigate(Routes.CREATE_MIX) },
                 onMixClick = { mixId -> navController.navigate(Routes.mixDetailRoute(mixId)) }
            )
            */
        }

        // --- 4. Rute CREATE MIX ---
        composable(Routes.CREATE_MIX) {
            // Di sini Anda akan memanggil Composable CreateMixScreen
            /*
            CreateMixRoute(
                 onSaveSuccess = {
                     // Kembali ke MyMix setelah Mix disimpan
                     navController.popBackStack()
                 }
            )
            */
        }

        // --- 5. Rute MIX DETAIL ---
        composable(
            route = Routes.MIX_DETAIL,
            arguments = listOf(navArgument("mixId") { type = NavType.IntType })
        ) { backStackEntry ->
            // Mengambil mixId dari rute
            val mixId = backStackEntry.arguments?.getInt("mixId") ?: return@composable

            // Di sini Anda akan memanggil Composable MixDetailScreen
            /*
            MixDetailRoute(mixId = mixId)
            */
        }
    }
}