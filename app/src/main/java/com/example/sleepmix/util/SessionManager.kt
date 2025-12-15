package com.example.sleepmix.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Extension untuk DataStore
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/**
 * Session Manager untuk menyimpan user yang sedang login
 */
class SessionManager(private val context: Context) {

    companion object {
        private val USER_ID_KEY = intPreferencesKey("user_id")
    }

    /**
     * Simpan user ID saat login
     */
    suspend fun saveUserId(userId: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    /**
     * Ambil user ID yang sedang login
     */
    val userIdFlow: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    /**
     * Hapus session saat logout
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }
}