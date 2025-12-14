package com.example.sleepmix.viewmodel.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sleepmix.repositori.MixRepository
import com.example.sleepmix.repositori.UserRepository
import com.example.sleepmix.viewmodel.LoginViewModel
import com.example.sleepmix.viewmodel.MixDetailViewModel
import com.example.sleepmix.viewmodel.RegistrasiViewModel

class RegistrasiViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrasiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistrasiViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LoginViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MixDetailViewModelFactory(
    private val mixRepository: MixRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MixDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MixDetailViewModel(mixRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}