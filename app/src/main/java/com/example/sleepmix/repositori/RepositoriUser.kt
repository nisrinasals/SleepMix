package com.example.sleepmix.repositori

import com.example.sleepmix.room.User
import com.example.sleepmix.room.dao.UserDao
import com.example.sleepmix.util.SessionManager
import kotlinx.coroutines.flow.Flow


interface UserRepository {
    suspend fun insertUser(user: User): Long
    suspend fun updateUser(user: User)
    suspend fun getUserByEmail(email: String): User?
    suspend fun getUserById(id: Int): User?
    suspend fun logoutAll()

    suspend fun saveSession(userId: Int)
    val currentUserId: Flow<Int?>
    suspend fun clearUserSession()
}

class OfflineUserRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : UserRepository {
    override suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    override suspend fun getUserByEmail(email: String): User? {
        // Digunakan untuk proses Login
        return userDao.getUserByEmail(email)
    }

    override suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }

    override suspend fun logoutAll() {
        // Digunakan untuk memastikan hanya satu user yang login
        userDao.logoutAll()
    }

    override suspend fun saveSession(userId: Int) {
        sessionManager.saveUserId(userId)
    }

    override val currentUserId: Flow<Int?> = sessionManager.userIdFlow

    override suspend fun clearUserSession() {
        // Clear session di DataStore
        sessionManager.clearSession()
        // Opsi: Update status isLoggedIn di Room ke false (jika diperlukan)
        userDao.logoutAll()
    }
}