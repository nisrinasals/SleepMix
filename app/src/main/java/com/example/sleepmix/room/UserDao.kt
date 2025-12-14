package com.example.sleepmix.room

import androidx.room.*

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM tblUser WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM tblUser WHERE userId = :id")
    suspend fun getUserById(id: Int): User?

    @Query("UPDATE tblUser SET isLoggedIn = 0")
    suspend fun logoutAll()
}
