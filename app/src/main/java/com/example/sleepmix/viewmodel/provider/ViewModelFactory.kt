package com.example.sleepmix.viewmodel.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sleepmix.repositori.MixRepository
import com.example.sleepmix.repositori.SoundRepository
import com.example.sleepmix.repositori.UserRepository
import com.example.sleepmix.viewmodel.*

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
            return MixDetailViewModel(mixRepository = mixRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

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

class BrowseSoundViewModelFactory(
    private val soundRepository: SoundRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BrowseSoundViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BrowseSoundViewModel(soundRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class EditMixViewModelFactory(
    private val mixRepository: MixRepository,
    private val soundRepository: SoundRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditMixViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditMixViewModel(
                mixRepository = mixRepository,
                soundRepository = soundRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}