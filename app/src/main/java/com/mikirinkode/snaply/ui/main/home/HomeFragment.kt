package com.mikirinkode.snaply.ui.main.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.data.model.StoryEntity
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

    private lateinit var pagingAdapter: HomePagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                adapter = pagingAdapter.withLoadStateFooter(
                    footer = LoadingStateAdapter {
                        pagingAdapter.retry()
                    }
                )
                pagingAdapter.addLoadStateListener { combinedLoadStates ->
                    val state = combinedLoadStates.mediator?.refresh.toString()
                    if (state.contains("Error", ignoreCase = true)) {
                        showLoading(false)
                        val parsedErrorMessage =
                            state.substringAfterLast("error=").substringBeforeLast(")")
                        if (state.contains("Unable to resolve host", ignoreCase = true)) {
                            showOnError(getString(R.string.txt_no_internet_desc))
                        } else {
                            showOnError(parsedErrorMessage)
                        }
                    }
                }
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
        showLoading(true)
        storyViewModel.getStories().observe(viewLifecycleOwner) {
            if (it.equals(PagingData.empty<StoryEntity>())){
                binding.emptyMessage.visibility = View.VISIBLE
            } else {
                binding.emptyMessage.visibility = View.GONE
            }

            binding.swipeToRefresh.isRefreshing = false
            pagingAdapter.submitData(lifecycle, it)
            showLoading(false)
        }
    }


    private fun showLoading(state: Boolean) {
        binding.apply {
            if (state) {
                loading.visibility = View.VISIBLE
                errorMessage.visibility = View.GONE
                shinyLoading.visibility = View.VISIBLE
            } else {
                loading.visibility = View.GONE
                shinyLoading.visibility = View.GONE
            }
        }
    }

    private fun showOnError(message: String) {
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
                        Pair(ivUserPhoto, getString(R.string.txt_transition_user_photo_profile)),
                        Pair(tvUserName, getString(R.string.txt_user_name))
                    )
                startActivity(
                    Intent(requireContext(), ProfileActivity::class.java),
                    optionsCompat.toBundle()
                )
            }

            swipeToRefresh.setOnRefreshListener {
                pagingAdapter.refresh()
                showLoading(true)
            }

            btnRetry.setOnClickListener {
                errorMessage.visibility = View.GONE
                pagingAdapter.retry()
                showLoading(true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}