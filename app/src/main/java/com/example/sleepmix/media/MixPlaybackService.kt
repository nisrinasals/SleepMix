package com.example.sleepmix.media

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.sleepmix.MainActivity
import com.example.sleepmix.repositori.AplikasiSleepMix
import com.example.sleepmix.repositori.MixRepository
import com.example.sleepmix.room.MixSound
import kotlinx.coroutines.*

// Ganti "Service" menjadi "MediaSessionService"
class MixPlaybackService : MediaSessionService() {

    private lateinit var audioController: AudioController
    private lateinit var mixRepository: MixRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    // MediaSession yang akan berinteraksi dengan sistem Android
    private var mediaSession: MediaSession? = null

    // Player dummy atau player utama, diperlukan oleh MediaSession
    private lateinit var dummyPlayer: ExoPlayer

    override fun onCreate() {
        super.onCreate()

        audioController = AudioController(applicationContext)
        mixRepository = AplikasiSleepMix.container.mixRepository

        // Player dummy untuk MediaSession (AudioController yang sebenarnya memutar)
        dummyPlayer = createDummyPlayer(applicationContext)

        // 1. Bangun MediaSession
        mediaSession = MediaSession.Builder(this, dummyPlayer)
            .setId("SleepMixSession")
            // Mengatur Intent yang akan dibuka saat notifikasi diklik
            .setSessionActivity(getSessionActivityPendingIntent())
            .build()

        // Catatan: Foreground Service akan otomatis dikelola oleh MediaSessionService
        // berdasarkan status pemutaran dummyPlayer.
    }

    // Fungsi untuk membuat PendingIntent yang membuka MainActivity
    private fun getSessionActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            // Flags untuk menghindari pembuatan instance Activity baru
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

    // Player Dummy: Diperlukan oleh MediaSession, tetapi tidak benar-benar memutar audio.
    private fun createDummyPlayer(context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build().apply {
            setVolume(0f)
            playWhenReady = false
        }
    }

    // 2. Wajib Override: Memberikan MediaSession kepada sistem
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    // 3. Service Connection masih diperlukan untuk interaksi langsung ViewModel -> AudioController
    private val binder = LocalBinder()
    inner class LocalBinder : android.os.Binder() {
        fun getService(): MixPlaybackService = this@MixPlaybackService
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Panggil parent untuk memungkinkan MediaSessionService bekerja
        super.onBind(intent)
        // Kembalikan binder lokal untuk interaksi langsung dari ViewModel
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

    // Fungsi yang dipanggil dari ViewModel (melalui Binder)

    fun startMix(mixId: Int) {
        serviceScope.launch {
            // Logika untuk memuat MixWithSounds dan memulai audioController tetap di sini
            // ...

            // PENTING: Untuk menampilkan notifikasi, kita harus "memutar" dummyPlayer
            dummyPlayer.playWhenReady = true
            dummyPlayer.prepare()
        }
    }

    fun stopAll() {
        audioController.stopAllPlayers()
        // Menghentikan dummyPlayer akan menghapus notifikasi
        dummyPlayer.playWhenReady = false
        dummyPlayer.stop()
        stopSelf()
    }

    fun setSoundVolume(mixSound: MixSound, newVolumeFloat: Float) {
        audioController.setVolume(mixSound.mixSoundId, newVolumeFloat)

        // Update volume ke database (sudah ada di ViewModel, tapi bisa juga di sini)
        // ...
    }
}