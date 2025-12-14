package com.example.sleepmix.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblSound")
data class Sound(
    @PrimaryKey(autoGenerate = true)
    val soundId : Int = 0,
    val name : String,
    val filePath : String,
    val iconRes : Int
)
