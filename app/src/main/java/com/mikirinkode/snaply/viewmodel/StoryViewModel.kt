package com.mikirinkode.snaply.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mikirinkode.snaply.data.StoryRepository
import com.mikirinkode.snaply.data.model.StoryEntity
import com.mikirinkode.snaply.data.remote.ApiService
import com.mikirinkode.snaply.data.remote.response.PostStoryResponse
import com.mikirinkode.snaply.data.remote.response.StoryResponse
import com.mikirinkode.snaply.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(private val storyRepository: StoryRepository) :
    ViewModel() {

    fun getStoryList(token: String) = storyRepository.getStoryList(token)


    fun addNewStory(token: String, imageMultipart: MultipartBody.Part, description: RequestBody) =
        storyRepository.addNewStory(token, imageMultipart, description)

}