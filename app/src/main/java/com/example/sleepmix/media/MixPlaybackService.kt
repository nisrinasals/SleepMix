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

class MixPlaybackService : MediaSessionService() {

    private lateinit var audioController: AudioController
    private lateinit var mixRepository: MixRepository
    private lateinit var soundRepository: SoundRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var mediaSession: MediaSession? = null
    private lateinit var dummyPlayer: ExoPlayer

    override fun onCreate() {
        super.onCreate()

        audioController = AudioController(applicationContext)
        mixRepository = AplikasiSleepMix.container.mixRepository
        soundRepository = AplikasiSleepMix.container.soundRepository

        dummyPlayer = createDummyPlayer(applicationContext)

        mediaSession = MediaSession.Builder(this, dummyPlayer)
            .setId("SleepMixSession")
            .setSessionActivity(getSessionActivityPendingIntent())
            .build()
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
        return binder
    }

    override fun onDestroy() {
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
     * CRITICAL FIX: Start Mix with proper sound loading
     */
    fun startMix(mixId: Int) {
        Log.d("MixPlaybackService", "startMix called for mixId: $mixId")

        serviceScope.launch {
            try {
                // 1. Load Mix with Sounds
                var mixWithSounds: com.example.sleepmix.room.MixWithSounds? = null
                mixRepository.getMixWithSoundsById(mixId).collect { data ->
                    mixWithSounds = data
                }

                if (mixWithSounds == null) {
                    Log.e("MixPlaybackService", "Mix not found: $mixId")
                    return@launch
                }

                Log.d("MixPlaybackService", "Loaded mix: ${mixWithSounds!!.mix.mixName}")
                Log.d("MixPlaybackService", "Number of sounds: ${mixWithSounds!!.sounds.size}")

                // 2. Load all Sound details
                val allSounds = soundRepository.getAllSounds()
                val soundMap = allSounds.associateBy { it.soundId }

                // 3. Start each sound in the mix
                mixWithSounds!!.sounds.forEach { mixSound ->
                    val sound = soundMap[mixSound.soundId]
                    if (sound != null) {
                        Log.d("MixPlaybackService", "Starting sound: ${sound.name}, volume: ${mixSound.volumeLevel}")

                        // Use sound's file path from database
                        audioController.startSound(mixSound, sound.filePath)
                    } else {
                        Log.e("MixPlaybackService", "Sound not found for soundId: ${mixSound.soundId}")
                    }
                }

                // 4. Activate dummy player for notification
                dummyPlayer.playWhenReady = true
                dummyPlayer.prepare()

                Log.d("MixPlaybackService", "✅ Mix playback started successfully")

            } catch (e: Exception) {
                Log.e("MixPlaybackService", "Error starting mix", e)
            }
        }
    }

    fun stopAll() {
        Log.d("MixPlaybackService", "stopAll called")
        audioController.stopAllPlayers()
        dummyPlayer.playWhenReady = false
        dummyPlayer.stop()
        stopSelf()
    }

    fun setSoundVolume(mixSound: MixSound, newVolumeFloat: Float) {
        Log.d("MixPlaybackService", "setSoundVolume: ${mixSound.mixSoundId}, volume: $newVolumeFloat")
        audioController.setVolume(mixSound.mixSoundId, newVolumeFloat)
    }
}