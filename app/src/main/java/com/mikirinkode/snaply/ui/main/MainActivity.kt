package com.mikirinkode.snaply.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.data.Result
import com.mikirinkode.snaply.databinding.ActivityMainBinding
import com.mikirinkode.snaply.ui.addstory.AddStoryActivity
import com.mikirinkode.snaply.ui.profile.ProfileActivity
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

    private val mainViewModel: MainViewModel by viewModels()
    private val storyAdapter = StoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        observeStoryList()

        mainViewModel.isLoading.observe(this) { showLoading(it) }
        mainViewModel.isError.observe(this) { error ->
            if (error) {
                mainViewModel.responseMessage.observe(this) {
                    it.getContentIfNotHandled()?.let { msg ->
                        showError(msg)
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.apply {
            rvStory.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                setHasFixedSize(true)
                adapter = storyAdapter
            }

            tvUserName.text = preferences.getStringValues(Preferences.USER_NAME)

            ivUserPhoto.setOnClickListener {
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@MainActivity,
                        Pair(ivUserPhoto, getString(R.string.user_photo_profile)),
                        Pair(tvUserName, getString(R.string.user_name))
                    )
                startActivity(
                    Intent(this@MainActivity, ProfileActivity::class.java),
                    optionsCompat.toBundle()
                )
            }

            btnAdd.setOnClickListener {
                startActivity(Intent(this@MainActivity, AddStoryActivity::class.java))
            }

            swipeToRefresh.setOnRefreshListener { observeStoryList() }

            btnRetry.setOnClickListener {
                errorMessage.visibility = View.GONE
                observeStoryList()
            }
        }
    }


    private fun observeStoryList() {
        val userToken = preferences.getStringValues(Preferences.USER_TOKEN)
        if (userToken != null) {
            mainViewModel.getStoryList(userToken).observe(this) { result ->
                when (result) {
                    is Result.Success -> {
                        binding.loading.visibility = View.GONE
                        if (result.data.isNotEmpty()) {
                            Log.d(TAG, result.data.size.toString())
                            storyAdapter.setData(result.data)
                        }
                    }
                    is Result.Loading -> {
                        binding.loading.visibility = View.VISIBLE
                    }
                    is Result.Error -> {
                        binding.loading.visibility = View.GONE
                        Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

//        mainViewModel.storyList.observe(this) { list ->
//            if (!list.isNullOrEmpty()) {
//                Log.d(TAG, list.size.toString())
//                storyAdapter.setData(list)
//            }
//        }
    }


    private fun showLoading(state: Boolean) {
        binding.apply {
            if (state) {
                loading.visibility = View.VISIBLE
                errorMessage.visibility = View.GONE
            } else {
                loading.visibility = View.GONE
            }
            if (state) shinyLoading.visibility = View.VISIBLE else shinyLoading.visibility =
                View.GONE
        }
    }

    private fun showError(message: String) {
        binding.apply {
            tvErrorDesc.text = message
            errorMessage.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(getString(R.string.exit_app))
        builder.setMessage(getString(R.string.are_you_sure_want_to_exit))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.sure)) { _, _ ->
                finish()
            }
            .setNegativeButton(getString(R.string.no)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}