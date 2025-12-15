package com.example.sleepmix.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.sleepmix.room.MixSound

/**
 * Mengelola koleksi MediaPlayer untuk Sound Blending.
 * FIXED: Proper error handling and resource management
 */
class AudioController(private val context: Context) {

    // Map untuk menyimpan pemain (Player) berdasarkan ID MixSound
    private val activePlayers = mutableMapOf<Int, MediaPlayer>()

    /**
     * Memulai pemutaran satu Mix Sound atau mengontrol yang sudah ada.
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
     * Memulai pemutaran audio baru dengan error handling yang lebih baik
     */
    private fun startSound(mixSound: MixSound, soundFilePath: String) {
        val mixSoundId = mixSound.mixSoundId

        Log.d("AudioController", "Starting sound: mixSoundId=$mixSoundId, path=$soundFilePath")

        try {
            val player = MediaPlayer().apply {
                // Set data source
                try {
                    setDataSource(soundFilePath)
                } catch (e: Exception) {
                    Log.e("AudioController", "Failed to set data source", e)

                    // Try alternative method: extract resource ID
                    val resourceId = soundFilePath.substringAfterLast("/").toIntOrNull()
                    if (resourceId != null && resourceId > 0) {
                        Log.d("AudioController", "Trying resource ID: $resourceId")
                        val afd = context.resources.openRawResourceFd(resourceId)
                        if (afd != null) {
                            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            afd.close()
                        } else {
                            throw Exception("Cannot open resource: $resourceId")
                        }
                    } else {
                        throw e
                    }
                }

                // Set Volume
                val volume = mixSound.volumeLevel.coerceIn(0f, 1f)
                setVolume(volume, volume)

                Log.d("AudioController", "Volume set to: $volume")

                // Set Looping
                isLooping = true

                // Error listener
                setOnErrorListener { mp, what, extra ->
                    Log.e("AudioController", "MediaPlayer error: what=$what, extra=$extra")
                    stopSound(mixSoundId)
                    false
                }

                // Prepare dan Play
                try {
                    prepare()
                    start()
                    Log.d("AudioController", "Sound started successfully: $mixSoundId")
                } catch (e: Exception) {
                    Log.e("AudioController", "Failed to prepare/start", e)
                    throw e
                }
            }

            activePlayers[mixSoundId] = player

        } catch (e: Exception) {
            Log.e("AudioController", "Error starting sound", e)
            // Don't crash, just skip this sound
        }
    }

    /**
     * Menghentikan dan melepaskan pemain untuk sound tertentu
     */
    fun stopSound(mixSoundId: Int) {
        Log.d("AudioController", "Stopping sound: $mixSoundId")
        activePlayers.remove(mixSoundId)?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            } catch (e: Exception) {
                Log.e("AudioController", "Error stopping sound", e)
            }
        }
    }

    /**
     * Menghentikan dan melepaskan SEMUA pemain
     */
    fun stopAllPlayers() {
        Log.d("AudioController", "Stopping all players")
        activePlayers.values.forEach { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            } catch (e: Exception) {
                Log.e("AudioController", "Error in stopAllPlayers", e)
            }
        }
        activePlayers.clear()
    }

    /**
     * Menyesuaikan volume sound tertentu secara real-time
     */
    fun setVolume(mixSoundId: Int, volumeLevel: Float) {
        val volume = volumeLevel.coerceIn(0f, 1f)
        activePlayers[mixSoundId]?.setVolume(volume, volume)
        Log.d("AudioController", "Volume updated: mixSoundId=$mixSoundId, volume=$volume")
    }

    /**
     * Check apakah sound sedang dimainkan
     */
    fun isPlaying(mixSoundId: Int): Boolean {
        return activePlayers[mixSoundId]?.isPlaying ?: false
    }

    /**
     * Get jumlah active players
     */
    fun getActivePlayersCount(): Int {
        return activePlayers.size
    }
}