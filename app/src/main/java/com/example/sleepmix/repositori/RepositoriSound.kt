package com.example.sleepmix.repositori

import com.example.sleepmix.room.Sound
import com.example.sleepmix.room.dao.SoundDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf // Menggunakan flowOf karena data sound mungkin statis setelah di-load

interface SoundRepository {
    // Digunakan untuk inisialisasi awal database
    suspend fun insertAllSounds(sounds: List<Sound>)
    // Data sound bawaan biasanya statis, jadi List<Sound> sudah cukup
    suspend fun getAllSounds(): List<Sound>
    suspend fun getSoundById(id: Int): Sound?
}

class OfflineSoundRepository(private val soundDao: SoundDao) : SoundRepository {
    override suspend fun insertAllSounds(sounds: List<Sound>) {
        soundDao.insertAll(sounds)
    }

    override suspend fun getAllSounds(): List<Sound> {
        return soundDao.getAllSounds()
    }

    override suspend fun getSoundById(id: Int): Sound? {
        return soundDao.getSoundById(id)
    }
}