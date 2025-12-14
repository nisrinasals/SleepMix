package com.example.sleepmix.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sleepmix.room.Mix

@Dao
interface MixDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(mix: Mix): Long

    @Update
    suspend fun update(mix: Mix)

    @Delete
    suspend fun delete(mix: Mix)

    @Query("SELECT * FROM tblMix WHERE userId = :userId")
    suspend fun getMixByUser(userId: Int): List<Mix>

    @Query("SELECT * FROM tblMix WHERE mixId = :mixId")
    suspend fun getMixById(mixId: Int): Mix?
}