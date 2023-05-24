package com.mikirinkode.snaply.ui.main.home

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.data.model.StoryEntity
import com.mikirinkode.snaply.databinding.ItemStoryBinding
import com.mikirinkode.snaply.ui.detail.DetailActivity

class HomePagingAdapter(private val activity: Activity): PagingDataAdapter<StoryEntity, HomePagingAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: StoryEntity) {
            binding.apply {
                tvStoryTitle.text = story.name

                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .apply(RequestOptions.placeholderOf(R.drawable.ic_image_loading))
                    .error(R.drawable.ic_broken_image)
                    .into(ivStoryPhoto)
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_STORY, story)

                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        Pair(binding.ivStoryPhoto, itemView.context.getString(R.string.content_desc_story_image)),
                    )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)

        if (data != null) {
            holder.bind(data)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}