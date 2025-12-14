package com.example.sleepmix.repositori

import android.app.Application
import android.content.Context
import com.example.sleepmix.room.SleepMixDatabase
import com.example.sleepmix.room.dao.*


interface AppContainer {
    // Repository untuk data user
    val userRepository: UserRepository
    // Repository untuk data sound bawaan
    val soundRepository: SoundRepository
    // Repository untuk data mix buatan user
    val mixRepository: MixRepository
}

// Asumsi: Semua DAO dan Repository diimpor dengan benar

class ContainerDataApp(private val context: Context) : AppContainer {

    // 1. INISIALISASI ROOM DATABASE
    // Room Database harus diinisialisasi secara lazy di level Application.
    private val database: SleepMixDatabase by lazy {
        SleepMixDatabase.getDatabase(context)
    }

    // 2. INISIALISASI DAO
    private val userDao: UserDao by lazy { database.userDao() }
    private val soundDao: SoundDao by lazy { database.soundDao() }
    private val mixDao: MixDao by lazy { database.mixDao() }
    private val mixSoundDao: MixSoundDao by lazy { database.mixSoundDao() }

    // 3. INISIALISASI REPOSITORY (Implementasi Interface AppContainer)

    // Menyediakan OfflineUserRepository
    override val userRepository: UserRepository by lazy {
        OfflineUserRepository(userDao)
    }

    // Menyediakan OfflineSoundRepository
    override val soundRepository: SoundRepository by lazy {
        OfflineSoundRepository(soundDao)
    }

    // Menyediakan OfflineMixRepository
    override val mixRepository: MixRepository by lazy {
        OfflineMixRepository(mixDao, mixSoundDao)
    }
}

/**
 * Kelas Application utama untuk menginisialisasi Container dependensi.
 */
class AplikasiSleepMix : Application() {

    // companion object untuk akses Singleton ke AppContainer
    companion object {
        // Digunakan untuk memudahkan akses Container dari luar
        lateinit var container: AppContainer
    }

    override fun onCreate() {
        super.onCreate()

        // Menginisialisasi ContainerDataApp saat aplikasi dimulai
        container = ContainerDataApp(this)

        // Catatan: Jika Anda memiliki SoundDao, ini adalah tempat yang baik
        // untuk menginisialisasi data Sound bawaan (jika belum ada).
        // Anda perlu CoroutineScope di sini atau memindahkannya ke tempat lain.
    }
}

