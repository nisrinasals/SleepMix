package com.example.sleepmix.media

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.sleepmix.room.MixSound

/**
 * Audio Controller dengan fade-in/fade-out
 * Sesuai SRS REQ-2.3: "Penambahan dan pengurangan layer harus menggunakan
 * fade-in/fade-out effect (durasi 300-500ms) untuk smooth transition"
 */
class AudioController(private val context: Context) {

    private val activePlayers = mutableMapOf<Int, MediaPlayer>()
    private val targetVolumes = mutableMapOf<Int, Float>()  // Store target volume for each player
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val FADE_DURATION_MS = 400L  // 400ms sesuai SRS (300-500ms range)
        private const val FADE_INTERVAL_MS = 50L   // Update setiap 50ms
    }

    /**
     * Start sound dengan fade-in effect
     * REQ-2.3: fade-in effect 300-500ms
     */
    fun startSound(mixSound: MixSound, soundFilePath: String) {
        val mixSoundId = mixSound.mixSoundId

        Log.d("AudioController", "startSound with fade: mixSoundId=$mixSoundId")

        try {
            val resourceId = soundFilePath.substringAfterLast("/").toIntOrNull()

            if (resourceId == null || resourceId == 0) {
                Log.e("AudioController", "Invalid resource ID from path: $soundFilePath")
                return
            }

            val player = MediaPlayer.create(context, resourceId)

            if (player == null) {
                Log.e("AudioController", "MediaPlayer.create() returned null for resource: $resourceId")
                return
            }

            // Store target volume
            val targetVolume = mixSound.volumeLevel.coerceIn(0f, 1f)
            targetVolumes[mixSoundId] = targetVolume

            player.apply {
                // Start dengan volume 0 untuk fade-in
                setVolume(0f, 0f)
                isLooping = true

                setOnErrorListener { _, what, extra ->
                    Log.e("AudioController", "MediaPlayer error: what=$what, extra=$extra")
                    stopSound(mixSoundId)
                    true
                }

                setOnCompletionListener {
                    Log.d("AudioController", "Sound completed: $mixSoundId")
                    stopSound(mixSoundId)
                }

                start()
                Log.d("AudioController", "✅ Sound started, beginning fade-in: $mixSoundId")
            }

            // Release old player jika ada
            activePlayers[mixSoundId]?.let { oldPlayer ->
                try {
                    fadeOutAndRelease(oldPlayer)
                } catch (e: Exception) {
                    Log.e("AudioController", "Error releasing old player", e)
                }
            }

            activePlayers[mixSoundId] = player

            // Fade-in ke target volume - REQ-2.3
            fadeIn(player, targetVolume)

        } catch (e: Exception) {
            Log.e("AudioController", "Error starting sound", e)
        }
    }

    /**
     * Fade-in effect
     * REQ-2.3: 300-500ms duration
     */
    private fun fadeIn(player: MediaPlayer, targetVolume: Float) {
        val steps = (FADE_DURATION_MS / FADE_INTERVAL_MS).toInt()
        val volumeIncrement = targetVolume / steps
        var currentStep = 0

        val fadeRunnable = object : Runnable {
            override fun run() {
                try {
                    if (!player.isPlaying) return

                    currentStep++
                    val newVolume = (volumeIncrement * currentStep).coerceAtMost(targetVolume)
                    player.setVolume(newVolume, newVolume)

                    if (currentStep < steps) {
                        handler.postDelayed(this, FADE_INTERVAL_MS)
                    } else {
                        Log.d("AudioController", "✅ Fade-in completed")
                    }
                } catch (e: Exception) {
                    Log.e("AudioController", "Error in fade-in", e)
                }
            }
        }

        handler.post(fadeRunnable)
    }

    /**
     * Stop sound dengan fade-out effect
     * REQ-2.3: fade-out effect 300-500ms
     */
    fun stopSound(mixSoundId: Int) {
        Log.d("AudioController", "Stopping sound with fade: $mixSoundId")

        activePlayers.remove(mixSoundId)?.let { player ->
            targetVolumes.remove(mixSoundId)
            fadeOutAndRelease(player)
        }
    }

    /**
     * Fade-out dan release player
     */
    private fun fadeOutAndRelease(player: MediaPlayer) {
        try {
            if (!player.isPlaying) {
                player.release()
                return
            }

            // Get current volume (approximate - use 1f as default)
            val startVolume = 1f
            val steps = (FADE_DURATION_MS / FADE_INTERVAL_MS).toInt()
            val volumeDecrement = startVolume / steps
            var currentStep = 0

            val fadeRunnable = object : Runnable {
                override fun run() {
                    try {
                        currentStep++
                        val newVolume = (startVolume - (volumeDecrement * currentStep)).coerceAtLeast(0f)
                        player.setVolume(newVolume, newVolume)

                        if (currentStep < steps && player.isPlaying) {
                            handler.postDelayed(this, FADE_INTERVAL_MS)
                        } else {
                            // Fade complete, release player
                            try {
                                if (player.isPlaying) {
                                    player.stop()
                                }
                                player.release()
                                Log.d("AudioController", "✅ Fade-out completed and released")
                            } catch (e: Exception) {
                                Log.e("AudioController", "Error releasing player after fade", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AudioController", "Error in fade-out", e)
                        try {
                            player.release()
                        } catch (ex: Exception) {
                            // Ignore
                        }
                    }
                }
            }

            handler.post(fadeRunnable)

        } catch (e: Exception) {
            Log.e("AudioController", "Error in fadeOutAndRelease", e)
            try {
                player.release()
            } catch (ex: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Stop all players dengan fade-out
     */
    fun stopAllPlayers() {
        Log.d("AudioController", "Stopping all players with fade (${activePlayers.size} active)")

        val playersToStop = activePlayers.values.toList()
        activePlayers.clear()
        targetVolumes.clear()

        playersToStop.forEach { player ->
            fadeOutAndRelease(player)
        }

        Log.d("AudioController", "✅ All players stopping with fade")
    }

    /**
     * Set volume - REQ-3.3: real-time
     */
    fun setVolume(mixSoundId: Int, volumeLevel: Float) {
        val volume = volumeLevel.coerceIn(0f, 1f)
        targetVolumes[mixSoundId] = volume

        activePlayers[mixSoundId]?.let { player ->
            try {
                player.setVolume(volume, volume)
                Log.d("AudioController", "Volume updated: mixSoundId=$mixSoundId, volume=$volume")
            } catch (e: Exception) {
                Log.e("AudioController", "Error setting volume", e)
            }
        }
    }

    fun isPlaying(mixSoundId: Int): Boolean {
        return activePlayers[mixSoundId]?.isPlaying ?: false
    }

    fun getActivePlayersCount(): Int {
        return activePlayers.size
    }

    /**
     * Cleanup - call from Service onDestroy
     */
    fun cleanup() {
        Log.d("AudioController", "🧹 Cleanup called")
        handler.removeCallbacksAndMessages(null)
        stopAllPlayers()
    }
}
