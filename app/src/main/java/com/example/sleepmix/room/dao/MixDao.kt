package com.example.sleepmix.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sleepmix.room.Mix
import com.example.sleepmix.room.MixWithSounds
import kotlinx.coroutines.flow.Flow

@Dao
interface MixDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(mix: Mix): Long

    @Update
    suspend fun update(mix: Mix)

    @Delete
    suspend fun delete(mix: Mix)

    // Menggunakan Flow agar data di MyMix List (View MyMix) selalu ter-update secara real-time
    @Query("SELECT * FROM tblMix WHERE userId = :userId")
    fun getMixesByUserIdStream(userId: Int): Flow<List<MixWithSounds>>
// TIDAK PERLU lagi menggunakan suspend fun getMixByUser, ganti dengan Flow di atas

    // Mengambil Mix lengkap beserta MixSound untuk halaman Mix Detail/Edit
    @Query("SELECT * FROM tblMix WHERE mixId = :mixId")
    fun getMixWithSoundsById(mixId: Int): Flow<MixWithSounds?>
// TIDAK PERLU lagi menggunakan suspend fun getMixById, ganti dengan Flow di atas
}