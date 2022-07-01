package com.mikirinkode.snaply.ui.main

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.data.model.StoryEntity
import com.mikirinkode.snaply.databinding.RvStoryBinding
import com.mikirinkode.snaply.ui.DetailActivity
import com.mikirinkode.snaply.utils.StoryDiffUtil
import androidx.core.util.Pair

class StoryAdapter : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    private var storyList = ArrayList<StoryEntity>()

    inner class StoryViewHolder(private val binding: RvStoryBinding) :
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
                        itemView.context as Activity,
                        Pair(binding.ivStoryPhoto, itemView.context.getString(R.string.story_image)),
                    )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = RvStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(storyList[position])
    }

    override fun getItemCount(): Int = storyList.size

    fun setData(newList: List<StoryEntity>){
        val diffUtil = StoryDiffUtil(storyList, newList)
        val diffResults = DiffUtil.calculateDiff(diffUtil)
        this.storyList.clear()
        this.storyList.addAll(newList)
        diffResults.dispatchUpdatesTo(this)
    }
}