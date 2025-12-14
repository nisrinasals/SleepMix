package com.example.sleepmix.room

import androidx.room.*

@Dao
interface MixDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMix(mix: Mix): Long

    @Update
    suspend fun updateMix(mix: Mix)

    @Delete
    suspend fun deleteMix(mix: Mix)

    @Query("SELECT * FROM tblMix WHERE userId = :userId")
    suspend fun getMixByUser(userId: Int): List<Mix>

    @Query("SELECT * FROM tblMix WHERE mixId = :mixId")
    suspend fun getMixById(mixId: Int): Mix?
}
