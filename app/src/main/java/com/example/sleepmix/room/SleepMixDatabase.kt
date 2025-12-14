package com.example.sleepmix.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sleepmix.room.dao.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Sound::class,
        Mix::class,
        MixSound::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SleepMixDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun soundDao(): SoundDao
    abstract fun mixDao(): MixDao
    abstract fun mixSoundDao(): MixSoundDao

    companion object {
        @Volatile
        private var INSTANCE: SleepMixDatabase? = null

        fun getDatabase(context: Context): SleepMixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SleepMixDatabase::class.java,
                    "sleepmix_database"
                )
                    .addCallback(SleepMixDatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
    private class SleepMixDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // Jalankan seeding di Coroutine Scope terpisah
            // Gunakan IO Dispatcher karena ini adalah operasi I/O database
            CoroutineScope(Dispatchers.IO).launch {
                // Mendapatkan INSTANCE database yang baru dibuat
                val database = INSTANCE ?: return@launch

                // Ambil DAO Sound
                val soundDao = database.soundDao()

                // Masukkan data Sound awal
                val initialSounds = SoundSeeds.populateInitialSounds(context)
                soundDao.insertAll(initialSounds)
            }
        }
    }
}

object SoundSeeds {
    // Fungsi untuk mendapatkan ID resource dari nama file di folder 'raw'
    // Menggunakan Context untuk resolve ID
    fun getRawResourceId(context: Context, name: String): Int {
        // resource type 'raw' dan package name dari aplikasi
        return context.resources.getIdentifier(name, "raw", context.packageName)
    }

    // Fungsi untuk mendapatkan ID resource dari nama file di folder 'drawable'
    fun getDrawableResourceId(context: Context, name: String): Int {
        // resource type 'drawable'
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    fun populateInitialSounds(context: Context): List<Sound> {
        val sounds = listOf(
            Triple("Bird", "bird", "bird"), // Name, raw_file_name, drawable_file_name
            Triple("Cricket", "crickets", "cricket"),
            Triple("Rain", "rain", "rain"),
            Triple("Sea Waves", "sea", "sea"),
            Triple("Thunderstorm", "thunder", "thunder")
        )

        return sounds.mapIndexed { index, data ->
            val name = data.first
            val rawFileName = data.second
            val drawableFileName = data.third

            Sound(
                // SoundId 1, 2, 3...
                soundId = index + 1,
                name = name,
                // filePath akan disimpan sebagai string resource path untuk digunakan oleh Media Player
                filePath = "android.resource://${context.packageName}/${getRawResourceId(context, rawFileName)}",
                iconRes = getDrawableResourceId(context, drawableFileName) // resource ID untuk Composable
            )
        }
    }
}
