package com.mikirinkode.snaply.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mikirinkode.snaply.data.model.StoryEntity

@Database(
  entities = [StoryEntity::class],
  version = 1,
  exportSchema = false
)
abstract class SnaplyDatabase: RoomDatabase() {
    abstract fun snaplyDao(): SnaplyDao
}