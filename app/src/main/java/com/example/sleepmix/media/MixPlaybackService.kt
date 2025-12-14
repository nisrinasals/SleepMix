package com.example.sleepmix.media

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.sleepmix.repositori.AplikasiSleepMix
import com.example.sleepmix.repositori.MixRepository
import com.example.sleepmix.room.MixSound
import kotlinx.coroutines.*

class MixPlaybackService : Service() {

    // 1. Komponen Utama
    private lateinit var audioController: AudioController
    private lateinit var mixRepository: MixRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    // 2. Local Binder (untuk diakses oleh Activity/ViewModel)
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): MixPlaybackService = this@MixPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        // Inisialisasi AudioController dan Repository dari AppContainer
        audioController = AudioController(applicationContext)
        mixRepository = AplikasiSleepMix.container.mixRepository

        // PENTING: Untuk menjalankan audio di latar belakang, Service harus dimulai sebagai Foreground Service
        // Anda harus menambahkan kode untuk menampilkan Notifikasi di sini.
        // startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Logika untuk menerima perintah melalui Intent (misalnya, PLAY, PAUSE, STOP)
        return START_STICKY // Service akan dimulai ulang jika dimatikan sistem
    }

    fun stopAll() {
        audioController.stopAllPlayers() // Hentikan semua player di controller
        // Hentikan Foreground Service dan hapus notifikasi
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf() // Hentikan Service sepenuhnya
    }

    override fun onDestroy() {
        serviceScope.cancel()
        audioController.stopAllPlayers()
        super.onDestroy()
    }

    // 3. Fungsi yang dipanggil dari ViewModel (melalui Binder)

    /**
     * Memuat dan memulai Mix lengkap.
     */
    fun startMix(mixId: Int) {
        serviceScope.launch {
            // Ambil data Mix lengkap (MixWithSounds) dari database
            mixRepository.getMixWithSoundsById(mixId).collect { mixWithSounds ->
                if (mixWithSounds != null) {
                    audioController.stopAllPlayers() // Hentikan yang lama

                    mixWithSounds.sounds.forEach { mixSound ->
                        // Ambil Sound (untuk mendapatkan filePath)
                        val sound = AplikasiSleepMix.container.soundRepository.getSoundById(mixSound.soundId)
                        if (sound != null) {
                            // Memulai setiap sound dalam Mix
                            audioController.toggleSound(mixSound, sound.filePath)
                        }
                    }
                }
            }
        }
    }

    /**
     * Mengganti volume sound tertentu secara real-time dan menyimpan ke DB.
     */
    fun setSoundVolume(mixSound: MixSound, newVolumeFloat: Float) {
        audioController.setVolume(mixSound.mixSoundId, newVolumeFloat)

        // Update volume ke database (hanya jika Anda memiliki MixSound tunggal update)
        serviceScope.launch {
            // PENTING: Anda perlu menambahkan fungsi updateMixSound(MixSound) ke MixRepository
            // mixRepository.updateMixSound(mixSound.copy(volumeLevel = newVolumeFloat))
            // Jika MixSound.volumeLevel adalah Int (0-100): mixSound.copy(volumeLevel = (newVolumeFloat * 100).toInt()))
        }
    }
}