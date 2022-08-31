package com.mikirinkode.snaply.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity(tableName = "story")
@Parcelize
data class StoryEntity(
	@PrimaryKey
	val id: String,
	val photoUrl: String,
	val createdAt: String,
	val name: String,
	val description: String,
	val lon: Double,
	val lat: Double
): Parcelable

