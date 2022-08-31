package com.mikirinkode.snaply.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mikirinkode.snaply.data.model.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SnaplyDao {

    @Query("SELECT * FROM story")
    fun getAllStory(): LiveData<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllStory(storyList: List<StoryEntity>)

    @Query("DELETE FROM story")
    fun deleteAll()
}