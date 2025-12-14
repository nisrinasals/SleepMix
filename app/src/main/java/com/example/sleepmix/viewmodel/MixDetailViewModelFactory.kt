package com.example.sleepmix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sleepmix.repositori.MixRepository

class MixDetailViewModelFactory(
    private val mixRepository: MixRepository
) : ViewModelProvider.Factory {

    // Metode utama untuk membuat instance ViewModel
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MixDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MixDetailViewModel(
                mixRepository = mixRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}