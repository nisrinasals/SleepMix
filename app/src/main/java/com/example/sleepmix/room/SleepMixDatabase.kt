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

/**
 * SleepMix Database - Sesuai SRS Section 4.3
 * Version 4: Updated schema dengan category, duration, lastModified
 */
@Database(
    entities = [
        User::class,
        Sound::class,
        Mix::class,
        MixSound::class
    ],
    version = 4,  // INCREASED VERSION untuk schema update
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
                        Log.d("Database", "Sound: ${sound.name}, Category: ${sound.category}, Duration: ${sound.duration}s")
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

/**
 * SoundSeeds - Data suara bawaan
 * Sesuai SRS REQ-1.1: "Sistem harus menyediakan minimal 8 jenis suara alam berbeda"
 */
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
     * Populate initial sounds - Sesuai SRS Section 4.3
     * Dengan category dan duration sesuai schema baru
     */
    fun populateInitialSounds(context: Context): List<Sound> {
        // Format: (name, rawFileName, drawableFileName, category, durationSeconds)
        val sounds = listOf(
            SoundData("Bird", "bird", "bird", "Nature", 60),
            SoundData("Cricket", "cricket", "cricket", "Nature", 60),
            SoundData("Rain", "rain", "rain", "Weather", 60),
            SoundData("Sea Waves", "sea", "sea", "Nature", 60),
            SoundData("Thunderstorm", "thunder", "thunder", "Weather", 60),
            SoundData("Firewood", "firewood", "firewood", "Ambience", 60),
            SoundData("Frog", "frog", "frog", "Nature", 60),
            SoundData("River", "river", "river", "Nature", 60)
        )

        return sounds.mapIndexed { index, soundData ->
            val rawResId = getRawResourceId(context, soundData.rawFileName)
            val drawableResId = getDrawableResourceId(context, soundData.drawableFileName)

            val filePath = "android.resource://${context.packageName}/${rawResId}"

            Sound(
                soundId = index + 1,
                name = soundData.name,
                filePath = filePath,
                iconRes = drawableResId,
                category = soundData.category,
                duration = soundData.duration
            )
        }
    }

    /**
     * Helper data class untuk sound initialization
     */
    private data class SoundData(
        val name: String,
        val rawFileName: String,
        val drawableFileName: String,
        val category: String,
        val duration: Int
    )
}
