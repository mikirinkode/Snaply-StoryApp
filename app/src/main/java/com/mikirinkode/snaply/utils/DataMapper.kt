package com.mikirinkode.snaply.utils

import com.mikirinkode.snaply.data.model.StoryEntity
import com.mikirinkode.snaply.data.remote.response.ListStoryItem

object DataMapper {

    private fun mapStoryResponseToEntity(data: ListStoryItem): StoryEntity {
        return StoryEntity(
            data.photoUrl,
            data.createdAt,
            data.name,
            data.description,
            data.lon,
            data.id,
            data.lat
        )
    }

    fun mapStoryResponsesToEntities(data: List<ListStoryItem>): List<StoryEntity>{
        return data.map { mapStoryResponseToEntity(it) }
    }
}