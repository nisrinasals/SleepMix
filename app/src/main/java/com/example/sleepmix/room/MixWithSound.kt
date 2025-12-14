package com.example.sleepmix.room

import androidx.room.Embedded
import androidx.room.Relation

data class MixWithSounds(
    @Embedded val mix: Mix,
    @Relation(
        parentColumn = "mixId", // Kolom di Mix
        entityColumn = "mixId"  // Kolom di MixSound yang mereferensikannya
    )
    val sounds: List<MixSound>
)