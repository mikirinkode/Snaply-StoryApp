package com.mikirinkode.snaply.data.source.local

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mikirinkode.snaply.data.model.StoryEntity

@Dao
interface StoryDao {

    @Query("SELECT * FROM story")
    fun getAllStory(): PagingSource<Int, StoryEntity>

    @Query("SELECT * FROM story WHERE lat IS NOT NULL AND lon IS NOT NULL")
    fun getAllStoryWithLocation(): LiveData<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllStory(storyList: List<StoryEntity>)

    @Query("DELETE FROM story")
    fun deleteAll()
}