package com.example.sleepmix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sleepmix.repositori.MixRepository
import com.example.sleepmix.repositori.UserRepository

class MyMixViewModelFactory(
    private val mixRepository: MixRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyMixViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyMixViewModel(
                mixRepository = mixRepository,
                userRepository = userRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}