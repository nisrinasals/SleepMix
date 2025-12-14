package com.example.sleepmix.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sleepmix.room.dao.*

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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
