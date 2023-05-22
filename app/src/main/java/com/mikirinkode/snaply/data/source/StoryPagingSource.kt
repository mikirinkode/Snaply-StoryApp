package com.mikirinkode.snaply.data.source

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mikirinkode.snaply.data.model.StoryEntity
import com.mikirinkode.snaply.data.source.remote.ApiService
import com.mikirinkode.snaply.data.source.remote.response.StoryResponseItem

class StoryPagingSource(
    private val token: String,
    private val apiService: ApiService,
): PagingSource<Int, StoryEntity>() {
    override fun getRefreshKey(state: PagingState<Int, StoryEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryEntity> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val response = apiService.getPagingStories("Bearer $token", position, params.loadSize)

            val storiesResponse = response.listStory
            val storyList = ArrayList<StoryEntity>()


            storiesResponse.forEach {
                val story = StoryEntity(
                    it.id,
                    it.photoUrl,
                    it.createdAt,
                    it.name,
                    it.description,
                    it.lon,
                    it.lat
                )
                storyList.add(story)
            }

            LoadResult.Page(
                data = storyList,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position -1,
                nextKey = if (storyList.isEmpty()) null else position + 1,
            )
        } catch (e: Exception) {
            Log.e("StoryPagingSource", "onError: $e")
            Log.e("StoryPagingSource", "onError: ${e.message}")
            return LoadResult.Error(e)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}