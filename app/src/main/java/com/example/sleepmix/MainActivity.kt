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

        // CRITICAL: Check and seed if needed
        lifecycleScope.launch {
            try {
                val soundRepository = AplikasiSleepMix.container.soundRepository
                val existingSounds = soundRepository.getAllSounds()

                Log.d("MainActivity", "Checking sounds: ${existingSounds.size} found")

                if (existingSounds.isEmpty()) {
                    Log.d("MainActivity", "Database empty, force seeding...")
                    val sounds = SoundSeeds.populateInitialSounds(applicationContext)
                    soundRepository.insertAllSounds(sounds)
                    Log.d("MainActivity", "✅ Seeded ${sounds.size} sounds")
                } else {
                    Log.d("MainActivity", "✅ Sounds already exist")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "❌ Error checking/seeding sounds", e)
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