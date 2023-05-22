package com.mikirinkode.snaply

import com.mikirinkode.snaply.data.model.StoryEntity

object DataDummy {
    fun generateDummyStoryResponses(): List<StoryEntity> {
        val items: MutableList<StoryEntity> = arrayListOf()
        for (i in 0..100){
            val story = StoryEntity(
                "$i",
                "$i url",
                "$i date",
                "$i name",
                "$i desc",
                i * 2.0,
                i * 3.0,
            )
            items.add(story)
        }
        return items
    }
}