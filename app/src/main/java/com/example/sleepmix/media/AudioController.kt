package com.example.sleepmix.media

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.sleepmix.room.MixSound // MixSound dari database

/**
 * Mengelola koleksi ExoPlayer untuk Sound Blending.
 */
class AudioController(private val context: Context) {

    // Map untuk menyimpan pemain (Player) berdasarkan ID MixSound
    private val activePlayers = mutableMapOf<Int, ExoPlayer>()

    /**
     * Memulai pemutaran satu Mix Sound atau mengontrol yang sudah ada.
     * @param mixSound Entitas MixSound yang berisi soundId dan volumeLevel.
     */
    fun toggleSound(mixSound: MixSound, soundFilePath: String) {
        val mixSoundId = mixSound.mixSoundId

        if (activePlayers.containsKey(mixSoundId)) {
            // Sound sedang dimainkan, HENTIKAN
            stopSound(mixSoundId)
        } else {
            // Sound belum dimainkan, MULAI
            startSound(mixSound, soundFilePath)
        }
    }

    /**
     * Memulai pemutaran audio baru.
     */
    private fun startSound(mixSound: MixSound, soundFilePath: String) {
        val mixSoundId = mixSound.mixSoundId
        val player = ExoPlayer.Builder(context).build().apply {
            // 1. Tentukan sumber audio (filePath di sini adalah Resource URI)
            val mediaItem = MediaItem.fromUri(Uri.parse(soundFilePath))
            setMediaItem(mediaItem)

            // 2. Set Volume Awal
            // volumeLevel di MixSound harus berupa Float (0.0f - 1.0f).
            // Anda harus memastikan entitas MixSound.volumeLevel diubah menjadi Float.
            val volume = mixSound.volumeLevel / 100.0f // Asumsi jika volumeLevel di DB masih Int(0-100)
            setVolume(volume)

            // 3. Set Looping (Putar berulang-ulang)
            repeatMode = Player.REPEAT_MODE_ALL

            // 4. Siapkan dan Putar
            prepare()
            playWhenReady = true
        }
        activePlayers[mixSoundId] = player
    }

    /**
     * Menghentikan dan melepaskan pemain (player) untuk sound tertentu.
     */
    fun stopSound(mixSoundId: Int) {
        activePlayers.remove(mixSoundId)?.let { player ->
            player.stop()
            player.release()
        }
    }

    /**
     * Menghentikan dan melepaskan SEMUA pemain (digunakan saat Service di-stop atau Mix selesai).
     */
    fun stopAllPlayers() {
        activePlayers.values.forEach { player ->
            player.stop()
            player.release()
        }
        activePlayers.clear()
    }

    /**
     * Menyesuaikan volume sound tertentu secara real-time.
     */
    fun setVolume(mixSoundId: Int, volumeLevel: Float) {
        activePlayers[mixSoundId]?.setVolume(volumeLevel)
        // Perubahan volume ini harus disusul dengan update ke database (melalui Repository)
    }
}