package com.mikirinkode.snaply.ui.main

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
class MainViewModel @Inject constructor(private val snapRepository: StoryRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _responseMessage = MutableLiveData<Event<String>>()
    val responseMessage: LiveData<Event<String>> = _responseMessage

    private val _storyList = MutableLiveData<ArrayList<StoryEntity>>()
    val storyList: LiveData<ArrayList<StoryEntity>> = _storyList

    fun getStoryList(token: String) = snapRepository.getStoryList(token)

    fun getAllStories(token: String) {
//        _isLoading.value = true
//        val client = api.getAllStories("Bearer $token", 20)
//        client.enqueue(object : Callback<StoryResponse> {
//            override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
//                _isLoading.value = false
//                _isError.value = !response.isSuccessful
//
//                if (response.isSuccessful) {
//                    Log.d(TAG, response.body()?.listStory?.size.toString())
//
//                    _responseMessage.value = Event(response.body()?.message.toString())
//
//                    if (!response.body()?.listStory.isNullOrEmpty()) {
//                        val list = ArrayList<StoryEntity>()
//                        response.body()?.listStory?.forEach {
//                            val story = StoryEntity(
//                                it.id,
//                                it.photoUrl,
//                                it.createdAt,
//                                it.name,
//                                it.description,
//                                it.lon,
//                                it.lat
//                            )
//                            list.add(story)
//                        }
//                        _storyList.postValue(list)
//                        Log.d(TAG, storyList.value?.size.toString())
//                    }
//                } else {
//                    _responseMessage.value = Event(response.message())
//                }
//            }
//
//            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
//                _isLoading.value = false
//                _isError.value = true
//                _responseMessage.value = Event(t.message.toString())
//                Log.e("$TAG(onFail)", t.message.toString())
//            }
//        })
    }

    fun addNewStory(token: String, imageMultipart: MultipartBody.Part, description: RequestBody){
//        _isLoading.value = true
//        api.addNewStory("Bearer $token", imageMultipart, description)
//            .enqueue(object : Callback<PostStoryResponse> {
//                override fun onResponse(
//                    call: Call<PostStoryResponse>,
//                    response: Response<PostStoryResponse>
//                ) {
//                    _isLoading.value = false
//                    _isError.value = !response.isSuccessful
//
//                    if (response.isSuccessful){
//                        _responseMessage.value = Event(response.body()?.message.toString())
//                    }else {
//                        _responseMessage.value = Event(response.message())
//                    }
//                }
//
//                override fun onFailure(call: Call<PostStoryResponse>, t: Throwable) {
//                    _isLoading.value = false
//                    _isError.value = true
//                    _responseMessage.value = Event(t.message.toString())
//                    Log.e("$TAG(onFail)", t.message.toString())
//                }
//            })
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}