package com.mikirinkode.snaply.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.snaply.databinding.ActivityMainBinding
import com.mikirinkode.snaply.ui.addstory.AddStoryActivity
import com.mikirinkode.snaply.ui.ProfileActivity
import com.mikirinkode.snaply.ui.auth.AuthViewModel
import com.mikirinkode.snaply.utils.Preferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var preferences: Preferences

    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val storyAdapter = StoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        observeUserData()
        observeStoryList()
        mainViewModel.isLoading.observe(this) { showLoading(it) }

        binding.apply {
            rvStory.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                setHasFixedSize(true)
                adapter = storyAdapter
            }

            tvUserName.text = preferences.getStringValues(Preferences.USER_NAME)

            ivUserPhoto.setOnClickListener {
                startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
            }

            btnAdd.setOnClickListener {
                startActivity(Intent(this@MainActivity, AddStoryActivity::class.java))
            }
        }
    }

    private fun observeStoryList() {
        mainViewModel.storyList.observe(this) { list ->
            if (!list.isNullOrEmpty()) {
                Log.d(TAG, list.size.toString())
                storyAdapter.setData(list)
            }
        }
    }

    private fun observeUserData() {

        val userEmail = preferences.getStringValues(Preferences.USER_EMAIL)
        val userPassword = preferences.getStringValues(Preferences.USER_PASSWORD)
        if (!userEmail.isNullOrEmpty() && !userPassword.isNullOrEmpty()) {
            authViewModel.loginUser(userEmail, userPassword)
        }

        authViewModel.userEntity.observe(this) {
            if (it != null) {
                // getAllStories
                mainViewModel.getAllStories(it.token)
                observeStoryList()
            }
        }
    }

    private fun showLoading(state: Boolean) {
        binding.apply {
            if (state) loading.visibility = View.VISIBLE else loading.visibility = View.GONE
            if (state) shinyLoading.visibility = View.VISIBLE else shinyLoading.visibility = View.GONE
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}