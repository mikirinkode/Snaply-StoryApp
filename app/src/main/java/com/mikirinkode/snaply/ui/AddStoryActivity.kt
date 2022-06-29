package com.mikirinkode.snaply.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.databinding.ActivityAddStoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddStoryActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAddStoryBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {

            btnBack.setOnClickListener { onBackPressed() }

            btnUpload.setOnClickListener {  }

            btnAddPhoto.setOnClickListener {  }
        }
    }
}