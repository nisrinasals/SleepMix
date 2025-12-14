package com.example.sleepmix.repositori

import com.example.sleepmix.room.dao.MixDao
import com.example.sleepmix.room.dao.MixSoundDao
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction // Untuk operasi yang melibatkan banyak tabel
import com.example.sleepmix.room.Mix
import com.example.sleepmix.room.MixSound
import com.example.sleepmix.room.MixWithSounds

interface MixRepository {
    fun getMixesByUserIdStream(userId: Int): Flow<List<MixWithSounds>>
    fun getMixWithSoundsById(mixId: Int): Flow<MixWithSounds?>

    // Menggabungkan INSERT Mix & semua MixSound dalam satu transaksi
    suspend fun createMix(mix: Mix, mixSounds: List<MixSound>): Long

    // Menggabungkan UPDATE Mix & MixSound dalam satu transaksi
    suspend fun updateMix(mix: Mix, mixSounds: List<MixSound>)

    // Menggabungkan DELETE Mix & MixSound dalam satu transaksi
    suspend fun deleteMix(mix: Mix)

    /**
     * Memperbarui MixSound tunggal (digunakan untuk menyimpan perubahan volume).
     */
    suspend fun updateMixSound(mixSound: MixSound)
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

    // --- Operasi Transaksi ---

    @Transaction
    override suspend fun createMix(mix: Mix, mixSounds: List<MixSound>): Long {
        // 1. Masukkan Mix utama, dapatkan mixId
        val newMixId = mixDao.insert(mix)

        // 2. Map daftar MixSound yang baru dengan mixId yang baru dibuat
        val soundsToInsert = mixSounds.map {
            it.copy(mixId = newMixId.toInt())
        }

        // 3. Masukkan semua MixSound terkait
        mixSoundDao.insertAll(soundsToInsert)

        return newMixId
    }

    @Transaction
    override suspend fun updateMix(mix: Mix, mixSounds: List<MixSound>) {
        // 1. Update Mix utama (mixName, dll.)
        mixDao.update(mix)

        // 2. Hapus semua MixSound lama untuk Mix ini
        mixSoundDao.deleteByMixId(mix.mixId)

        // 3. Masukkan daftar MixSound baru (yang mungkin telah dimodifikasi)
        mixSoundDao.insertAll(mixSounds)
    }

    @Transaction
    override suspend fun deleteMix(mix: Mix) {
        // 1. Hapus semua MixSound yang terkait
        mixSoundDao.deleteByMixId(mix.mixId)

        // 2. Hapus Mix utama
        mixDao.delete(mix)
    }

    @Transaction
    override suspend fun updateMixSound(mixSound: MixSound) {
        // Memanggil fungsi update dari MixSoundDao
        mixSoundDao.updateMixSound(mixSound)
    }
}