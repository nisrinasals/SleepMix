package com.example.sleepmix.media

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.sleepmix.MainActivity
import com.example.sleepmix.R
import com.example.sleepmix.repositori.AplikasiSleepMix
import com.example.sleepmix.repositori.MixRepository
import com.example.sleepmix.room.MixSound
import kotlinx.coroutines.*

class MixPlaybackService : Service() {

    // 1. Komponen Utama
    private lateinit var audioController: AudioController
    private lateinit var mixRepository: MixRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    // 2. Notification
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "sleepmix_playback_channel"
    private var currentMixName = "Sleep Mix"

    // 3. Local Binder (untuk diakses oleh Activity/ViewModel)
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): MixPlaybackService = this@MixPlaybackService
    }

    override fun onCreate() {
        super.onCreate()

        // Inisialisasi AudioController dan Repository
        audioController = AudioController(applicationContext)
        mixRepository = AplikasiSleepMix.container.mixRepository

        // Buat Notification Channel (untuk Android O+)
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Logika untuk menerima perintah melalui Intent
        return START_STICKY
    }

    /**
     * Membuat Notification Channel untuk Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SleepMix Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows currently playing sleep mix"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Membuat Notification untuk Foreground Service
     */
    private fun buildNotification(): Notification {
        // Intent untuk membuka aplikasi saat notification diklik
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SleepMix Playing")
            .setContentText("Currently playing: $currentMixName")
            .setSmallIcon(android.R.drawable.ic_media_play) // Ganti dengan icon custom
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    /**
     * Memuat dan memulai Mix lengkap
     */
    fun startMix(mixId: Int) {
        serviceScope.launch {
            mixRepository.getMixWithSoundsById(mixId).collect { mixWithSounds ->
                if (mixWithSounds != null) {
                    currentMixName = mixWithSounds.mix.mixName

                    // Start Foreground Service dengan Notification
                    startForeground(NOTIFICATION_ID, buildNotification())

                    audioController.stopAllPlayers() // Hentikan yang lama

                    mixWithSounds.sounds.forEach { mixSound ->
                        val sound = AplikasiSleepMix.container.soundRepository
                            .getSoundById(mixSound.soundId)
                        if (sound != null) {
                            audioController.toggleSound(mixSound, sound.filePath)
                        }
                    }
                }
            }
        }
    }

    /**
     * Mengganti volume sound tertentu secara real-time
     */
    fun setSoundVolume(mixSound: MixSound, newVolumeFloat: Float) {
        audioController.setVolume(mixSound.mixSoundId, newVolumeFloat)

        serviceScope.launch {
            try {
                mixRepository.updateMixSound(mixSound.copy(volumeLevel = newVolumeFloat))
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    /**
     * Menghentikan semua pemutaran
     */
    fun stopAll() {
        audioController.stopAllPlayers()

        // Stop Foreground Service dan hapus notifikasi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }

        stopSelf()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        audioController.stopAllPlayers()
        super.onDestroy()
    }
}