package com.example.sleepmix.media

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.sleepmix.MainActivity
import com.example.sleepmix.repositori.AplikasiSleepMix
import com.example.sleepmix.repositori.MixRepository
import com.example.sleepmix.repositori.SoundRepository
import com.example.sleepmix.room.MixSound
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first  // ✅ CORRECT IMPORT

/**
 * FIXED MixPlaybackService
 * Using kotlinx.coroutines.flow.first() instead of custom extension
 */
class MixPlaybackService : MediaSessionService() {

    private lateinit var audioController: AudioController
    private lateinit var mixRepository: MixRepository
    private lateinit var soundRepository: SoundRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var mediaSession: MediaSession? = null
    private lateinit var dummyPlayer: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        Log.d("MixPlaybackService", "🟢 onCreate called")

        try {
            audioController = AudioController(applicationContext)
            mixRepository = AplikasiSleepMix.container.mixRepository
            soundRepository = AplikasiSleepMix.container.soundRepository

            dummyPlayer = createDummyPlayer(applicationContext)

            mediaSession = MediaSession.Builder(this, dummyPlayer)
                .setId("SleepMixSession")
                .setSessionActivity(getSessionActivityPendingIntent())
                .build()

            Log.d("MixPlaybackService", "✅ Service initialized successfully")
        } catch (e: Exception) {
            Log.e("MixPlaybackService", "❌ Error in onCreate", e)
        }
    }

    private fun getSessionActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
    }

    private fun createDummyPlayer(context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build().apply {
            setVolume(0f)
            playWhenReady = false
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    private val binder = LocalBinder()
    inner class LocalBinder : android.os.Binder() {
        fun getService(): MixPlaybackService = this@MixPlaybackService
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        Log.d("MixPlaybackService", "🔗 onBind called")
        return binder
    }

    override fun onDestroy() {
        Log.d("MixPlaybackService", "🔴 onDestroy called")
        serviceScope.cancel()
        audioController.stopAllPlayers()
        mediaSession?.run {
            release()
            mediaSession = null
        }
        dummyPlayer.release()
        super.onDestroy()
    }

    /**
     * ✅ CORRECT VERSION: Using kotlinx.coroutines.flow.first()
     */
    fun startMix(mixId: Int) {
        Log.d("MixPlaybackService", "🎵 startMix called for mixId: $mixId")

        serviceScope.launch {
            try {
                Log.d("MixPlaybackService", "📥 Step 1: Loading mix data...")

                // ✅ CORRECT: Use kotlinx.coroutines.flow.first() from standard library
                val mixWithSounds = withContext(Dispatchers.IO) {
                    mixRepository.getMixWithSoundsById(mixId).first()
                }

                if (mixWithSounds == null) {
                    Log.e("MixPlaybackService", "❌ Mix not found or null: $mixId")
                    return@launch
                }

                Log.d("MixPlaybackService", "✅ Step 2: Loaded mix: ${mixWithSounds.mix.mixName}")
                Log.d("MixPlaybackService", "   Number of sounds: ${mixWithSounds.sounds.size}")

                if (mixWithSounds.sounds.isEmpty()) {
                    Log.w("MixPlaybackService", "⚠️ Mix has no sounds!")
                    return@launch
                }

                // Load all Sound details
                Log.d("MixPlaybackService", "📥 Step 3: Loading sound details...")
                val allSounds = withContext(Dispatchers.IO) {
                    soundRepository.getAllSounds()
                }
                Log.d("MixPlaybackService", "📚 Loaded ${allSounds.size} sounds from database")

                val soundMap = allSounds.associateBy { it.soundId }

                // Start each sound in the mix
                Log.d("MixPlaybackService", "🎵 Step 4: Starting sounds...")
                mixWithSounds.sounds.forEachIndexed { index, mixSound ->
                    val sound = soundMap[mixSound.soundId]
                    if (sound != null) {
                        Log.d("MixPlaybackService", "▶️ [$index] Starting: ${sound.name}")
                        Log.d("MixPlaybackService", "   Volume: ${mixSound.volumeLevel}")
                        Log.d("MixPlaybackService", "   Path: ${sound.filePath}")

                        // Use sound's file path from database
                        audioController.startSound(mixSound, sound.filePath)
                    } else {
                        Log.e("MixPlaybackService", "❌ Sound not found for soundId: ${mixSound.soundId}")
                    }
                }

                // Activate dummy player for notification
                Log.d("MixPlaybackService", "📱 Step 5: Activating dummy player...")
                dummyPlayer.playWhenReady = true
                dummyPlayer.prepare()

                val activeCount = audioController.getActivePlayersCount()
                Log.d("MixPlaybackService", "✅ Mix playback started successfully!")
                Log.d("MixPlaybackService", "   Active players: $activeCount")

                if (activeCount == 0) {
                    Log.e("MixPlaybackService", "⚠️ WARNING: No sounds actually started!")
                }

            } catch (e: CancellationException) {
                Log.w("MixPlaybackService", "⚠️ startMix cancelled")
                throw e  // Re-throw to properly cancel coroutine
            } catch (e: Exception) {
                Log.e("MixPlaybackService", "❌ Error starting mix", e)
                e.printStackTrace()
            }
        }
    }

    fun stopAll() {
        Log.d("MixPlaybackService", "⏹️ stopAll called")
        audioController.stopAllPlayers()
        dummyPlayer.playWhenReady = false
        dummyPlayer.stop()
        Log.d("MixPlaybackService", "✅ All stopped")
    }

    fun setSoundVolume(mixSound: MixSound, newVolumeFloat: Float) {
        Log.d("MixPlaybackService", "🔊 setSoundVolume: ${mixSound.mixSoundId}, volume: $newVolumeFloat")
        audioController.setVolume(mixSound.mixSoundId, newVolumeFloat)
    }
}