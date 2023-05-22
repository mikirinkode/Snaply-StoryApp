package com.mikirinkode.snaply.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mikirinkode.snaply.data.model.RemoteKeys
import com.mikirinkode.snaply.data.model.StoryEntity

@Database(
  entities = [StoryEntity::class, RemoteKeys::class],
  version = 2,
  exportSchema = false
)
abstract class SnaplyDatabase: RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}