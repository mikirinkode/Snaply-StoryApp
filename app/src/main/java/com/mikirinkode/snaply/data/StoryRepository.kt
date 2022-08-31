package com.mikirinkode.snaply.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.mikirinkode.snaply.data.local.SnaplyDao
import com.mikirinkode.snaply.data.model.StoryEntity
import com.mikirinkode.snaply.data.remote.ApiService
import com.mikirinkode.snaply.data.remote.response.StoryResponse
import com.mikirinkode.snaply.utils.AppExecutors
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class StoryRepository @Inject constructor(
    private val apiService: ApiService,
    private val snaplyDao: SnaplyDao,
    private val appExecutors: AppExecutors
) {
    private val result = MediatorLiveData<Result<List<StoryEntity>>>()

    fun getStoryList(token: String): LiveData<Result<List<StoryEntity>>> {
//        val resultList = MutableLiveData<Result<List<StoryEntity>>>()

        result.value = Result.Loading
        val client = apiService.getAllStories("Bearer $token", 20)
        client.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                if (response.isSuccessful) {
                    if (!response.body()?.listStory.isNullOrEmpty()) {
                        val list = ArrayList<StoryEntity>()
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
                            snaplyDao.deleteAll()
                            snaplyDao.insertAllStory(list)
                        }
                    }
                } else {
//                    _responseMessage.value = Event(response.message())
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                result.value = Result.Error(t.message.toString())
                Log.e("${TAG}(onFail)", t.message.toString())
            }
        })

        // check local data
        val localData = snaplyDao.getAllStory()
        result.addSource(localData) { newData: List<StoryEntity> ->
            result.value = Result.Success(newData)
        }

        return result
    }

    companion object {
        private const val TAG = "StoryRepository"

        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService,
            snaplyDao: SnaplyDao,
            appExecutors: AppExecutors
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, snaplyDao, appExecutors)
            }.also { instance = it }
    }
}