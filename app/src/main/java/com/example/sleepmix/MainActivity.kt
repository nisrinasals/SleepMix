package com.example.sleepmix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import com.example.sleepmix.ui.theme.SleepMixTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.sleepmix.navigation.MainNavigation


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hapus enableEdgeToEdge() atau ganti dengan implementasi terbaru

        setContent {
            SleepMixTheme {
                // Menggunakan Surface sebagai wadah utama dan memanggil Navigasi
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation() // Panggil Main Navigation
                }
            }
        }
    }
}