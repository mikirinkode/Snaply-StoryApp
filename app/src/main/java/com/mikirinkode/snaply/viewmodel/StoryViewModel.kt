package com.mikirinkode.snaply.viewmodel

import android.util.Log
import androidx.lifecycle.*
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

    private val stories: LiveData<PagingData<StoryEntity>>
            = storyRepository.getPagingStory().cachedIn(viewModelScope)

    private val storiesWithLocation = storyRepository.getStoryList(1)

    fun getStories(): LiveData<PagingData<StoryEntity>> {
        return stories
    }

    fun getStoryWithLocationList(): LiveData<Result<List<StoryEntity>>> {
        return storiesWithLocation
    }

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