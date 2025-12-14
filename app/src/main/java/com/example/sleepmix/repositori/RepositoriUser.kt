package com.example.sleepmix.repositori

import com.example.sleepmix.room.User
import com.example.sleepmix.room.dao.UserDao


interface UserRepository {
    suspend fun insertUser(user: User): Long
    suspend fun updateUser(user: User)
    suspend fun getUserByEmail(email: String): User?
    suspend fun getUserById(id: Int): User?
    suspend fun logoutAll()
}

class OfflineUserRepository(private val userDao: UserDao) : UserRepository {
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
}