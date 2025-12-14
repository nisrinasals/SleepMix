package com.example.sleepmix.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sleepmix.room.MixSound

@Dao
interface MixSoundDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertMixSound(mixSound: MixSound): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(mixSounds: List<MixSound>)

    @Update
    suspend fun updateMixSound(mixSound: MixSound)

    @Delete
    suspend fun deleteMixSound(mixSound: MixSound)

    @Query("SELECT * FROM tblMixSound WHERE mixId = :mixId")
    suspend fun getSoundsByMix(mixId: Int): List<MixSound>

    @Query("DELETE FROM tblMixSound WHERE mixId = :mixId")
    suspend fun deleteByMixId(mixId: Int)
}