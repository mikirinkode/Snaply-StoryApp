package com.mikirinkode.snaply.ui.auth

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mikirinkode.snaply.data.model.UserEntity
import com.mikirinkode.snaply.data.remote.ApiService
import com.mikirinkode.snaply.data.remote.response.LoginResponse
import com.mikirinkode.snaply.data.remote.response.RegisterResponse
import com.mikirinkode.snaply.utils.Event
import com.mikirinkode.snaply.utils.Preferences
import com.mikirinkode.snaply.utils.RestError
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: ApiService,
    private val preferences: Preferences
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _responseMessage = MutableLiveData<Event<String>>()
    val responseMessage: LiveData<Event<String>> = _responseMessage

    private val _userEntity = MutableLiveData<UserEntity>()
    val userEntity: LiveData<UserEntity> = _userEntity

    fun registerNewUser(name: String, email: String, password: String) {
        _isLoading.value = true
        val client = api.registerNewUser(name, email, password)
        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                _isLoading.value = false
                _isError.value = !response.isSuccessful

                if (response.isSuccessful) {
                    _responseMessage.value = Event(response.body()?.message.toString())
                } else {
                    _responseMessage.value = Event(response.message())
                    try {
                        val jObjError = JSONObject(response.errorBody().toString())
                        Log.e(TAG, jObjError.getJSONObject("error").getString("message"))
                    } catch (e: Exception) {
                        Log.e(TAG, e.message.toString())
                    }
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e("$TAG(onFail)", t.message.toString())
                _isLoading.value = false
                _isError.value = true
                _responseMessage.value = Event(t.message.toString())
            }

        })
    }

    fun loginUser(email: String, password: String) {
        _isLoading.value = true
        val client = api.loginUser(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _isLoading.value = false
                _isError.value = !response.isSuccessful

                Log.e("$TAG(Body)", response.body().toString())
                Log.e(TAG, response.message())

                if (response.isSuccessful) {
                    val loginResult = response.body()?.loginResult
                    _responseMessage.value = Event(response.body()?.message.toString())
                    if (loginResult != null) {
                        _userEntity.value =
                            UserEntity(loginResult.userId, loginResult.name, loginResult.token)
                        preferences.setValues(Preferences.USER_ID, loginResult.userId)
                        preferences.setValues(Preferences.USER_NAME, loginResult.name)
                        preferences.setValues(Preferences.USER_TOKEN, loginResult.token)
                    }
                } else {
                    _responseMessage.value = Event(response.message())

                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        Log.e(TAG, jObjError.getJSONObject("error").getString("message"))
                    } catch (e: java.lang.Exception) {
                        Log.e(TAG, e.message.toString())
                    }

                    try {
                        val gson = Gson()
                        val type = object : TypeToken<LoginResponse>() {}.type
                        var errorResponse: LoginResponse? =
                            gson.fromJson(response.errorBody()!!.charStream(), type)

                        Log.e(TAG, errorResponse?.message.toString())
                    } catch (e: Exception) {
                        Log.e(TAG, e.message.toString())
                    }

                    try {
                        val gson = Gson();
                        val errorMessage: RestError =
                            gson.fromJson(response.errorBody()?.charStream(), RestError::class.java)
                        Log.e(TAG, errorMessage.message.toString())
                        Log.e(TAG, errorMessage.error.toString())

                    } catch (e: Exception) {
                        Log.e(TAG, e.message.toString())
                    }

                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("$TAG(onFail)", t.message.toString())
                _responseMessage.value = Event(t.message.toString())
                _isLoading.value = false
                _isError.value = true
            }
        })
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}