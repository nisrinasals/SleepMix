package com.example.sleepmix.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tblMixSound",
    foreignKeys = [
        ForeignKey(
            entity = Mix::class,
            parentColumns = ["mixId"],
            childColumns = ["mixId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Sound::class,
            parentColumns = ["soundId"],
            childColumns = ["soundId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["mixId"]),
        Index(value = ["soundId"])
    ]
)
data class MixSound(
    @PrimaryKey(autoGenerate = true)
    val mixSoundId: Int = 0,
    val mixId: Int,
    val soundId: Int,
    val volumeLevel: Float
)
