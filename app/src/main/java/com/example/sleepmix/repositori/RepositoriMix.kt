package com.example.sleepmix.repositori

import com.example.sleepmix.room.dao.MixDao
import com.example.sleepmix.room.dao.MixSoundDao
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction
import com.example.sleepmix.room.Mix
import com.example.sleepmix.room.MixSound
import com.example.sleepmix.room.MixWithSounds

interface MixRepository {
    fun getMixesByUserIdStream(userId: Int): Flow<List<MixWithSounds>>
    fun getMixWithSoundsById(mixId: Int): Flow<MixWithSounds?>

    suspend fun createMix(mix: Mix, mixSounds: List<MixSound>): Long
    suspend fun updateMix(mix: Mix, mixSounds: List<MixSound>)
    suspend fun deleteMix(mix: Mix)
    suspend fun updateMixSound(mixSound: MixSound)

    // NEW: Delete individual MixSound
    suspend fun deleteMixSound(mixSound: MixSound)
}

class OfflineMixRepository(
    private val mixDao: MixDao,
    private val mixSoundDao: MixSoundDao
) : MixRepository {

    override fun getMixesByUserIdStream(userId: Int): Flow<List<MixWithSounds>> {
        return mixDao.getMixesByUserIdStream(userId)
    }

    override fun getMixWithSoundsById(mixId: Int): Flow<MixWithSounds?> {
        return mixDao.getMixWithSoundsById(mixId)
    }

    @Transaction
    override suspend fun createMix(mix: Mix, mixSounds: List<MixSound>): Long {
        val newMixId = mixDao.insert(mix)
        val soundsToInsert = mixSounds.map {
            it.copy(mixId = newMixId.toInt())
        }
        mixSoundDao.insertAll(soundsToInsert)
        return newMixId
    }

    @Transaction
    override suspend fun updateMix(mix: Mix, mixSounds: List<MixSound>) {
        mixDao.update(mix)
        mixSoundDao.deleteByMixId(mix.mixId)
        mixSoundDao.insertAll(mixSounds)
    }

    @Transaction
    override suspend fun deleteMix(mix: Mix) {
        mixSoundDao.deleteByMixId(mix.mixId)
        mixDao.delete(mix)
    }

    @Transaction
    override suspend fun updateMixSound(mixSound: MixSound) {
        mixSoundDao.updateMixSound(mixSound)
    }

    @Transaction
    override suspend fun deleteMixSound(mixSound: MixSound) {
        mixSoundDao.deleteMixSound(mixSound)
    }
}