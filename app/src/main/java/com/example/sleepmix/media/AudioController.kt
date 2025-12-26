package com.example.sleepmix.media

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.sleepmix.room.MixSound

class AudioController(private val context: Context) {

    private val activePlayers = mutableMapOf<Int, MediaPlayer>()

    fun startSound(mixSound: MixSound, soundFilePath: String) {
        val mixSoundId = mixSound.mixSoundId

        Log.d("AudioController", "startSound: mixSoundId=$mixSoundId")

        try {
            // Extract resource ID from path
            val resourceId = soundFilePath.substringAfterLast("/").toIntOrNull()

            if (resourceId == null || resourceId == 0) {
                Log.e("AudioController", "Invalid resource ID from path: $soundFilePath")
                return
            }

            Log.d("AudioController", "Using resource ID: $resourceId")

            // Create MediaPlayer
            val player = MediaPlayer.create(context, resourceId)

            if (player == null) {
                Log.e("AudioController", "MediaPlayer.create() returned null for resource: $resourceId")
                return
            }

            player.apply {
                // Set volume
                val volume = mixSound.volumeLevel.coerceIn(0f, 1f)
                setVolume(volume, volume)

                // Set looping
                isLooping = true

                // ✅ CRITICAL: Error listener
                setOnErrorListener { mp, what, extra ->
                    Log.e("AudioController", "MediaPlayer error: what=$what, extra=$extra")
                    stopSound(mixSoundId)
                    true  // Return true = error handled
                }

                // ✅ CRITICAL: Completion listener (shouldn't trigger with looping, but safety)
                setOnCompletionListener {
                    Log.d("AudioController", "Sound completed: $mixSoundId")
                    stopSound(mixSoundId)
                }

                // Start playback
                start()
                Log.d("AudioController", "✅ Sound started: $mixSoundId")
            }

            // ✅ CRITICAL: Release old player if exists
            activePlayers[mixSoundId]?.let { oldPlayer ->
                try {
                    if (oldPlayer.isPlaying) {
                        oldPlayer.stop()
                    }
                    oldPlayer.release()
                    Log.d("AudioController", "Released old player for: $mixSoundId")
                } catch (e: Exception) {
                    Log.e("AudioController", "Error releasing old player", e)
                }
            }

            activePlayers[mixSoundId] = player

        } catch (e: Exception) {
            Log.e("AudioController", "Error starting sound", e)
        }
    }

    fun stopSound(mixSoundId: Int) {
        Log.d("AudioController", "Stopping sound: $mixSoundId")
        activePlayers.remove(mixSoundId)?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()  // ✅ CRITICAL: Always release!
                Log.d("AudioController", "✅ Released player: $mixSoundId")
            } catch (e: Exception) {
                Log.e("AudioController", "Error stopping sound", e)
            }
        }
    }

    fun stopAllPlayers() {
        Log.d("AudioController", "Stopping all players (${activePlayers.size} active)")

        // ✅ Create copy to avoid ConcurrentModificationException
        val playersToStop = activePlayers.values.toList()
        activePlayers.clear()

        playersToStop.forEach { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()  // ✅ CRITICAL: Always release!
            } catch (e: Exception) {
                Log.e("AudioController", "Error in stopAllPlayers", e)
            }
        }

        Log.d("AudioController", "✅ All players released")
    }

    fun setVolume(mixSoundId: Int, volumeLevel: Float) {
        val volume = volumeLevel.coerceIn(0f, 1f)
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
     * ✅ NEW: Cleanup method to call from Service onDestroy
     */
    fun cleanup() {
        Log.d("AudioController", "🧹 Cleanup called")
        stopAllPlayers()
    }
}