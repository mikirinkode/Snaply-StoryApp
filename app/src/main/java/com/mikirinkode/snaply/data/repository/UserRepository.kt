package com.mikirinkode.snaply.data.repository

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import com.google.gson.Gson
import com.mikirinkode.snaply.data.Result
import com.mikirinkode.snaply.data.source.local.StoryDao
import com.mikirinkode.snaply.data.model.UserEntity
import com.mikirinkode.snaply.data.source.remote.ApiService
import com.mikirinkode.snaply.data.source.remote.response.LoginResponse
import com.mikirinkode.snaply.data.source.remote.response.RegisterResponse
import com.mikirinkode.snaply.utils.AppExecutors
import com.mikirinkode.snaply.utils.Preferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val storyDao: StoryDao,
    private val appExecutors: AppExecutors,
    private val preferences: Preferences
) {

    fun registerNewUser(name: String, email: String, password: String): MediatorLiveData<Result<String>> {
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading
        val client = apiService.registerNewUser(name, email, password)
        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                if (response.isSuccessful) {
                    result.value = Result.Success(response.body()?.message.toString())
                } else {
                    try {
                        val responseBody = Gson().fromJson(response.errorBody()?.charStream(), RegisterResponse::class.java)

                        Log.e(TAG, "response.errorBody()::" + responseBody.error.toString())
                        Log.e(TAG, "response.errorBody()::" + responseBody.message)

                        result.value = Result.Error(responseBody.message)
                    } catch (e: Exception){
                        Log.e(TAG, e.message.toString())
                    }
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e("${TAG}(onFail)", t.message.toString())
                result.value = Result.Error(t.message.toString())
            }

        })
        
        return result
    }

    fun loginUser(email: String, password: String): MediatorLiveData<Result<UserEntity>> {
        val result = MediatorLiveData<Result<UserEntity>>()
        result.value = Result.Loading

        val client = apiService.loginUser(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {

                if (response.isSuccessful) {
                    val loginResult = response.body()?.loginResult
                    if (loginResult != null) {

                        result.value = Result.Success(
                            UserEntity(
                                loginResult.userId,
                                loginResult.name,
                                loginResult.token
                            )
                        )
                        preferences.setValues(Preferences.USER_ID, loginResult.userId)
                        preferences.setValues(Preferences.USER_NAME, loginResult.name)
                        preferences.setValues(Preferences.USER_TOKEN, loginResult.token)
                    }
                } else {
                    try {
                        val responseBody = Gson().fromJson(
                            response.errorBody()?.charStream(),
                            LoginResponse::class.java
                        )

                        Log.e(TAG, "response.errorBody()::" + responseBody.error.toString())
                        Log.e(TAG, "response.errorBody()::" + responseBody.message)

                        result.value = Result.Error(responseBody.message.toString())
                    } catch (e: Exception) {
                        Log.e(TAG, e.message.toString())
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("${TAG}(onFail)", t.message.toString())
                result.value = Result.Error(t.message.toString())
            }
        })

        return result
    }

    companion object {
        private const val TAG = "UserRepository"
    }
}