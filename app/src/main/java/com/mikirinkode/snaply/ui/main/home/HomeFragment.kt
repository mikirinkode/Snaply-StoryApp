package com.mikirinkode.snaply.ui.main.home

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.data.Result
import com.mikirinkode.snaply.data.source.LoadingStateAdapter
import com.mikirinkode.snaply.databinding.FragmentHomeBinding
import com.mikirinkode.snaply.ui.profile.ProfileActivity
import com.mikirinkode.snaply.utils.Preferences
import com.mikirinkode.snaply.viewmodel.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferences: Preferences

    private val storyViewModel: StoryViewModel by viewModels()

    //    private lateinit var storyAdapter: StoryAdapter
    private lateinit var pagingAdapter: HomePagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for requireContext() fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initVariables()
        observeStoryList()
        initUi()
        onActionClick()
    }

    private fun initVariables() {
        pagingAdapter = HomePagingAdapter(requireActivity())
    }

    private fun initUi() {
        binding.apply {
            tvGreetings.text = getGreetings()

            rvStory.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
//                adapter = storyAdapter
                adapter = pagingAdapter.withLoadStateFooter(
                    footer = LoadingStateAdapter {
                        pagingAdapter.retry()
                    }
                )
            }

            tvUserName.text = preferences.getStringValues(Preferences.USER_NAME)
        }
    }

    private fun getGreetings(): String {
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)

        return when {
            currentHour < 12 -> "Good Morning,"
            currentHour < 18 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
    }

    private fun observeStoryList() {
        storyViewModel.stories.observe(viewLifecycleOwner) {
            binding.swipeToRefresh.isRefreshing = false
            pagingAdapter.submitData(lifecycle, it)
        }
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

    private fun onActionClick() {
        binding.apply {
            ivUserPhoto.setOnClickListener {
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        Pair(ivUserPhoto, getString(R.string.user_photo_profile)),
                        Pair(tvUserName, getString(R.string.user_name))
                    )
                startActivity(
                    Intent(requireContext(), ProfileActivity::class.java),
                    optionsCompat.toBundle()
                )
            }

            swipeToRefresh.setOnRefreshListener { observeStoryList() }

            btnRetry.setOnClickListener {
                errorMessage.visibility = View.GONE
                observeStoryList()
            }
        }
    }

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}