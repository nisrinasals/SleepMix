package com.example.sleepmix.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sleepmix.room.Sound

@Dao
interface SoundDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSound(sound: Sound): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(sounds: List<Sound>)

    @Update
    suspend fun updateSound(sound: Sound)

    @Delete
    suspend fun deleteSound(sound: Sound)

    @Query("SELECT * FROM tblSound")
    suspend fun getAllSounds(): List<Sound>

    @Query("SELECT * FROM tblSound WHERE soundId = :id")
    suspend fun getSoundById(id: Int): Sound?
}