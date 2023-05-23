package com.mikirinkode.snaply.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.paging.*
import com.google.gson.Gson
import com.mikirinkode.snaply.data.Result
import com.mikirinkode.snaply.data.source.local.StoryDao
import com.mikirinkode.snaply.data.model.StoryEntity
import com.mikirinkode.snaply.data.source.StoryRemoteMediator
import com.mikirinkode.snaply.data.source.local.SnaplyDatabase
import com.mikirinkode.snaply.data.source.remote.ApiService
import com.mikirinkode.snaply.data.source.remote.response.PostStoryResponse
import com.mikirinkode.snaply.data.source.remote.response.RegisterResponse
import com.mikirinkode.snaply.data.source.remote.response.StoryResponse
import com.mikirinkode.snaply.utils.AppExecutors
import com.mikirinkode.snaply.utils.Preferences
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class StoryRepository @Inject constructor(
    private val apiService: ApiService,
    private val snaplyDatabase: SnaplyDatabase,
    private val storyDao: StoryDao,
    private val preferences: Preferences,
    private val appExecutors: AppExecutors
) {

//            "Mediator Live Data adalah komponen yang digunakan untuk menggabungkan" +
//            "banyak sumber data dalam sebuah LiveData." +
//            "Apabila data BUKAN livedata -> gunakan setValue()" +
//            "Jika Live Data -> gunakan addsource()"
    private val result = MediatorLiveData<Result<List<StoryEntity>>>()

    fun getPagingStory(): LiveData<PagingData<StoryEntity>>{
        val userToken = preferences.getStringValues(Preferences.USER_TOKEN)
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator("Bearer $userToken", snaplyDatabase, apiService),
            pagingSourceFactory = {
                storyDao.getAllStory()
            }
        ).liveData
    }

    fun getStoryList(needLocation: Int): LiveData<Result<List<StoryEntity>>> {

        val userToken = preferences.getStringValues(Preferences.USER_TOKEN)

        result.value = Result.Loading
        val client = apiService.getAllStories("Bearer $userToken", 20, needLocation)
        client.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                if (response.isSuccessful) {
                    if (!response.body()?.listStory.isNullOrEmpty()) {
                        val list = ArrayList<StoryEntity>()

                        // TODO: CHECK WHY NEED APP EXECUTORS
                        appExecutors.diskIO.execute {
                            response.body()?.listStory?.forEach {
                                val story = StoryEntity(
                                    it.id,
                                    it.photoUrl,
                                    it.createdAt,
                                    it.name,
                                    it.description,
                                    it.lon,
                                    it.lat
                                )
                                list.add(story)
                            }
                            storyDao.deleteAll()
                            storyDao.insertAllStory(list)
                        }
                    }
                }else {
                    try {
                        val responseBody = Gson().fromJson(
                            response.errorBody()?.charStream(),
                            RegisterResponse::class.java
                        )
                        Log.e(TAG, "response.errorBody()::" + responseBody.message)

                        result.value = Result.Error(responseBody.message.toString())
                    } catch (e: Exception) {
                        Log.e(TAG, e.message.toString())
                    }
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                result.value = Result.Error(t.message.toString())
                Log.e("$TAG(onFail)", t.message.toString())
            }
        })

        // check local data
        val localData = storyDao.getAllStoryWithLocation()

        // apabila data sumber berupa live data, maka perlu menggunakan addSource,
        // jika bukan gunakan setValue
        result.addSource(localData) { newData: List<StoryEntity> ->
            result.value = Result.Success(newData)
        }

        return result
    }


    fun addNewStory(
        token: String,
        imageMultipart: MultipartBody.Part,
        description: RequestBody,
        latitude: RequestBody?,
        longitude: RequestBody?,
    ): MediatorLiveData<Result<String>> {
        val result = MediatorLiveData<Result<String>>()

        result.value = Result.Loading

        apiService.addNewStory("Bearer $token", imageMultipart, description, latitude, longitude)
            .enqueue(object : Callback<PostStoryResponse> {
                override fun onResponse(
                    call: Call<PostStoryResponse>,
                    response: Response<PostStoryResponse>
                ) {
                    if (response.isSuccessful) {
                        result.value = Result.Success(response.body()?.message.toString())
                    } else {
                        try {
                            val responseBody = Gson().fromJson(
                                response.errorBody()?.charStream(),
                                RegisterResponse::class.java
                            )
                            Log.e(TAG, "response.errorBody()::" + responseBody.message)

                            result.value = Result.Error(responseBody.message.toString())
                        } catch (e: Exception) {
                            Log.e(TAG, e.message.toString())
                        }
                    }
                }

                override fun onFailure(call: Call<PostStoryResponse>, t: Throwable) {
                    result.value = Result.Error(t.message.toString())
                    Log.e("$TAG(onFail)", t.message.toString())
                }
            })

        return result
    }

    companion object {
        private const val TAG = "StoryRepository"
    }
}