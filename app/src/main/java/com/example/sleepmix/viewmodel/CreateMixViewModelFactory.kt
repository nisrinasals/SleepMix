package com.example.sleepmix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sleepmix.repositori.MixRepository
import com.example.sleepmix.repositori.SoundRepository

class CreateMixViewModelFactory(
    private val mixRepository: MixRepository,
    private val soundRepository: SoundRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateMixViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateMixViewModel(
                mixRepository = mixRepository,
                soundRepository = soundRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}