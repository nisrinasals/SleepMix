package com.example.sleepmix.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblUser")
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId : Int = 0,
    val nama : String,
    val email : String,
    val password : String,
    val isLoggedIn : Boolean
)
