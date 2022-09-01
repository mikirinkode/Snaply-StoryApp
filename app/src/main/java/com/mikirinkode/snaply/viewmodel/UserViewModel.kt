package com.mikirinkode.snaply.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.mikirinkode.snaply.data.repository.UserRepository
import com.mikirinkode.snaply.data.source.remote.ApiService
import com.mikirinkode.snaply.data.source.remote.response.LoginResponse
import com.mikirinkode.snaply.data.source.remote.response.RegisterResponse
import com.mikirinkode.snaply.utils.Event
import com.mikirinkode.snaply.utils.Preferences
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    fun registerNewUser(name: String, email: String, password: String) =
        userRepository.registerNewUser(name, email, password)

    fun loginUser(email: String, password: String) = userRepository.loginUser(email, password)
}