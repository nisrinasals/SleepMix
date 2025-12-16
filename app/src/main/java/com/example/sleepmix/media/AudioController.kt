package com.example.sleepmix.media

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.sleepmix.room.MixSound

class AudioController(private val context: Context) {

    private val activePlayers = mutableMapOf<Int, MediaPlayer>()

    fun toggleSound(mixSound: MixSound, soundFilePath: String) {
        val mixSoundId = mixSound.mixSoundId

        if (activePlayers.containsKey(mixSoundId)) {
            stopSound(mixSoundId)
        } else {
            startSound(mixSound, soundFilePath)
        }
    }

    /**
     * CRITICAL FIX: Use MediaPlayer.create() for reliability
     */
    fun startSound(mixSound: MixSound, soundFilePath: String) {
        val mixSoundId = mixSound.mixSoundId

        Log.d("AudioController", "startSound: mixSoundId=$mixSoundId, path=$soundFilePath")

        try {
            // Extract resource ID from path
            // Format: "android.resource://com.example.sleepmix/2131689472"
            val resourceId = soundFilePath.substringAfterLast("/").toIntOrNull()

            if (resourceId == null || resourceId == 0) {
                Log.e("AudioController", "Invalid resource ID from path: $soundFilePath")
                return
            }

            Log.d("AudioController", "Using resource ID: $resourceId")

            // Use MediaPlayer.create() - more reliable
            val player = MediaPlayer.create(context, resourceId)

            if (player == null) {
                Log.e("AudioController", "MediaPlayer.create() returned null for resource: $resourceId")
                return
            }

            player.apply {
                // Set volume
                val volume = mixSound.volumeLevel.coerceIn(0f, 1f)
                setVolume(volume, volume)
                Log.d("AudioController", "Volume set to: $volume")

                // Set looping
                isLooping = true

                // Error listener
                setOnErrorListener { mp, what, extra ->
                    Log.e("AudioController", "MediaPlayer error: what=$what, extra=$extra")
                    stopSound(mixSoundId)
                    false
                }

                // Start playback
                start()
                Log.d("AudioController", "✅ Sound started: $mixSoundId")
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
                player.release()
            } catch (e: Exception) {
                Log.e("AudioController", "Error stopping sound", e)
            }
        }
    }

    fun stopAllPlayers() {
        Log.d("AudioController", "Stopping all players (${activePlayers.size} active)")
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

    fun setVolume(mixSoundId: Int, volumeLevel: Float) {
        val volume = volumeLevel.coerceIn(0f, 1f)
        activePlayers[mixSoundId]?.setVolume(volume, volume)
        Log.d("AudioController", "Volume updated: mixSoundId=$mixSoundId, volume=$volume")
    }

    fun isPlaying(mixSoundId: Int): Boolean {
        return activePlayers[mixSoundId]?.isPlaying ?: false
    }

    fun getActivePlayersCount(): Int {
        return activePlayers.size
    }
}