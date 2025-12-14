package com.example.sleepmix.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tblMix",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Mix(
    @PrimaryKey(autoGenerate = true)
    val mixId : Int = 0,
    val userId : Int,
    val creationDate : Long,
    val mixName : String
)