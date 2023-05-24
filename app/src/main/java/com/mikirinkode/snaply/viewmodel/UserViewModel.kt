package com.mikirinkode.snaply.viewmodel

import androidx.lifecycle.ViewModel
import com.mikirinkode.snaply.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    fun registerNewUser(name: String, email: String, password: String) =
        userRepository.registerNewUser(name, email, password)

    fun loginUser(email: String, password: String) = userRepository.loginUser(email, password)
}