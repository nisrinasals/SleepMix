package com.example.sleepmix.room

import androidx.room.*

@Dao
interface SoundDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSound(sound: Sound): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
