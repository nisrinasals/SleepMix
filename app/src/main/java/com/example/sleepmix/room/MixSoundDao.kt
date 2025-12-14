package com.example.sleepmix.room

import androidx.room.*

@Dao
interface MixSoundDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMixSound(mixSound: MixSound): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
