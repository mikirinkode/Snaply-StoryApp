package com.mikirinkode.snaply.viewmodel

import androidx.lifecycle.ViewModel
import com.mikirinkode.snaply.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(private val storyRepository: StoryRepository) :
    ViewModel() {

    fun getStoryList(token: String) = storyRepository.getStoryList(token)


    fun addNewStory(token: String, imageMultipart: MultipartBody.Part, description: RequestBody) =
        storyRepository.addNewStory(token, imageMultipart, description)

}