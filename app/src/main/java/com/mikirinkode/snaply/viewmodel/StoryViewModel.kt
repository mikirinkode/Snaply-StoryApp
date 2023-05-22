package com.mikirinkode.snaply.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mikirinkode.snaply.data.Result
import com.mikirinkode.snaply.data.model.StoryEntity
import com.mikirinkode.snaply.data.repository.StoryRepository
import com.mikirinkode.snaply.data.source.remote.response.StoryResponseItem
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(private val storyRepository: StoryRepository) :
    ViewModel() {

    fun getPagingStory(token: String): LiveData<PagingData<StoryEntity>> {
        return storyRepository.getPagingStory(token).cachedIn(viewModelScope)
    }

    fun getStoryList(token: String) = storyRepository.getStoryList(token, 0)

    fun getStoryWithLocationList(token: String) = storyRepository.getStoryList(token, 1)

    fun addNewStory(
        token: String,
        imageMultipart: MultipartBody.Part,
        description: RequestBody,
        latitude: RequestBody? = null,
        longitude: RequestBody? = null,
    ) =
        storyRepository.addNewStory(token, imageMultipart, description, latitude, longitude)

    fun addNewStory(
        token: String,
        imageMultipart: MultipartBody.Part,
        description: String,
        latitude: Double? = null,
        longitude: Double? = null,
    ): MediatorLiveData<Result<String>> {
        val desc = description.toRequestBody("text/plain".toMediaType())
        if (latitude == null && longitude == null){
            return storyRepository.addNewStory(token, imageMultipart, desc, null, null)
        } else {
            val lat = latitude.toString().toRequestBody("text/plain".toMediaType())
            val long = longitude.toString().toRequestBody("text/plain".toMediaType())
            return storyRepository.addNewStory(token, imageMultipart, desc, lat, long)
        }
    }

}