package com.example.sleepmix.room

import android.content.Context
import android.util.Log
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
    version = 2,  // INCREASED VERSION untuk force re-create
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
                    .fallbackToDestructiveMigration()
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

            Log.d("Database", "onCreate called - Starting seeding")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = INSTANCE ?: return@launch
                    val soundDao = database.soundDao()

                    val initialSounds = SoundSeeds.populateInitialSounds(context)

                    Log.d("Database", "Inserting ${initialSounds.size} sounds")
                    initialSounds.forEach { sound ->
                        Log.d("Database", "Sound: ${sound.name}, Path: ${sound.filePath}, Icon: ${sound.iconRes}")
                    }

                    soundDao.insertAll(initialSounds)
                    Log.d("Database", "✅ Seeding completed successfully")

                } catch (e: Exception) {
                    Log.e("Database", "❌ Seeding failed", e)
                }
            }
        }
    }
}

object SoundSeeds {
    /**
     * Get resource ID from raw folder
     */
    fun getRawResourceId(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "raw", context.packageName)
    }

    /**
     * Get resource ID from drawable folder
     */
    fun getDrawableResourceId(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    /**
     * Populate initial sounds dengan file path yang BENAR
     */
    fun populateInitialSounds(context: Context): List<Sound> {
        val sounds = listOf(
            Triple("Bird", "bird", "bird"),
            Triple("Cricket", "crickets", "cricket"),  // Note: audio = "crickets" but icon = "cricket"
            Triple("Rain", "rain", "rain"),
            Triple("Sea Waves", "sea", "sea"),
            Triple("Thunderstorm", "thunder", "thunder")
        )

        return sounds.mapIndexed { index, (name, rawFileName, drawableFileName) ->
            val rawResId = getRawResourceId(context, rawFileName)
            val drawableResId = getDrawableResourceId(context, drawableFileName)

            // CRITICAL FIX: Use correct URI format
            // Format LAMA (SALAH): "android.resource://package/resId"
            // Format BARU (BENAR): "android.resource://package/raw/resId"
            val filePath = "android.resource://${context.packageName}/${rawResId}"

            Sound(
                soundId = index + 1,
                name = name,
                filePath = filePath,
                iconRes = drawableResId
            )
        }
    }
}