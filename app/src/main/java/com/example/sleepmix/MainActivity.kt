package com.example.sleepmix

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.sleepmix.repositori.AplikasiSleepMix
import com.example.sleepmix.room.SoundSeeds
import com.example.sleepmix.ui.navigation.SleepMixNavigation
import com.example.sleepmix.ui.theme.SleepMixTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MainActivity.kt - onCreate()
        lifecycleScope.launch {
            val soundRepository = AplikasiSleepMix.container.soundRepository
            val sounds = soundRepository.getAllSounds()

            Log.d("MainActivity", "=== DATABASE CHECK ===")
            Log.d("MainActivity", "Total sounds in DB: ${sounds.size}")

            sounds.forEach { sound ->
                Log.d("MainActivity", "Sound: ${sound.name}, ID: ${sound.soundId}, Path: ${sound.filePath}")
            }

            if (sounds.isEmpty()) {
                Log.e("MainActivity", "❌ DATABASE EMPTY! Force seeding...")
                val initialSounds = SoundSeeds.populateInitialSounds(applicationContext)
                soundRepository.insertAllSounds(initialSounds)
                Log.d("MainActivity", "✅ Seeded ${initialSounds.size} sounds")
            }
        }

        enableEdgeToEdge()
        setContent {
            SleepMixTheme {
                SleepMixNavigation()
            }
        }
    }
}