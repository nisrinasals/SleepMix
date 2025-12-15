package com.example.sleepmix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.sleepmix.ui.navigation.SleepMixNavigation
import com.example.sleepmix.ui.theme.SleepMixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SleepMixTheme {
                SleepMixNavigation()
            }
        }
    }
}