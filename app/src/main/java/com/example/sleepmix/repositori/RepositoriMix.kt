package com.example.sleepmix.repositori

import com.example.sleepmix.room.Mix
import com.example.sleepmix.room.dao.MixDao
import kotlinx.coroutines.flow.Flow


interface RepositoriMix {
    fun getAllMixStream(): Flow<List<Mix>>
    suspend fun insertMix(mix: Mix): Long

    fun getMixStream(id: Int): Flow<Mix?>

    suspend fun deleteMix(mix: Mix)

    suspend fun updateMix(mix: Mix)
}

class OfflineRepositoriMix(
    private val mixDao: MixDao
): RepositoriMix{
    override fun getAllMixStream(): Flow<List<Mix>> = mixDao
        .getAllMix()
    override suspend fun insertMix(mix: Mix) = mixDao
        .insert(mix)
    override fun getMixStream(id: Int): Flow<Mix?> = mixDao.getMix(id)
    override suspend fun deleteMix(mix: Mix) = mixDao.delete(mix)
    override suspend fun updateMix(mix: Mix) = mixDao.update(mix)
}