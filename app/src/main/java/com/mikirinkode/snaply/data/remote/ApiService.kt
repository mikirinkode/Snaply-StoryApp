package com.mikirinkode.snaply.data.remote

import com.mikirinkode.snaply.data.remote.response.LoginResponse
import com.mikirinkode.snaply.data.remote.response.PostStoryResponse
import com.mikirinkode.snaply.data.remote.response.RegisterResponse
import com.mikirinkode.snaply.data.remote.response.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("register")
    fun registerNewUser(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<RegisterResponse>


    @FormUrlEncoded
    @POST("login")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<LoginResponse>

    @GET("stories")
    fun getAllStories(
        @Header("Authorization") token: String,
    ): Call<StoryResponse>

    @Multipart
    @POST("stories")
    fun addNewStory(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): Call<PostStoryResponse>
}