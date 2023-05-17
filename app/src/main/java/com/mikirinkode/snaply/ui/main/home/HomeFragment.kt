package com.mikirinkode.snaply.ui.main.home

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.data.Result
import com.mikirinkode.snaply.databinding.FragmentHomeBinding
import com.mikirinkode.snaply.ui.maps.MapsActivity
import com.mikirinkode.snaply.ui.profile.ProfileActivity
import com.mikirinkode.snaply.utils.Preferences
import com.mikirinkode.snaply.viewmodel.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferences: Preferences

    private val storyViewModel: StoryViewModel by viewModels()
    private lateinit var storyAdapter: StoryAdapter

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
        managePermissions()
        observeStoryList()
        initUi()
        onActionClick()
    }

    private fun initVariables() {
        storyAdapter = StoryAdapter(requireActivity())
    }

    private fun initUi(){
        binding.apply {
            rvStory.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = storyAdapter
            }

            tvUserName.text = preferences.getStringValues(Preferences.USER_NAME)
        }
    }

    private fun onActionClick(){
        binding.apply{
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

    private fun managePermissions() {
        // check the permissions
        val requestPermissions = mutableListOf<String>()
        if (!isLocationPermissionGranted()){
            // if permissions are not granted
            requestPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (requestPermissions.isNotEmpty()){
            // request the permission
            ActivityCompat.requestPermissions(requireActivity(), requestPermissions.toTypedArray(), LOCATION_REQUEST_CODE)
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fineLocation && coarseLocation
    }
    private fun observeStoryList() {
        val userToken = preferences.getStringValues(Preferences.USER_TOKEN)
        if (userToken != null) {
            storyViewModel.getStoryList(userToken).observe(viewLifecycleOwner) { result ->
                binding.swipeToRefresh.isRefreshing = false
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
                        Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
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

    // TODO
//    override fun onBackPressed() {
//        val builder = MaterialAlertDialogBuilder(requireContext())
//        builder.setTitle(getString(R.string.exit_app))
//        builder.setMessage(getString(R.string.are_you_sure_want_to_exit))
//            .setCancelable(false)
//            .setPositiveButton(getString(R.string.sure)) { _, _ ->
//                finish()
//            }
//            .setNegativeButton(getString(R.string.no)) { dialogInterface, _ ->
//                dialogInterface.cancel()
//            }
//
//        val alertDialog: AlertDialog = builder.create()
//        alertDialog.show()
//    }

    // handle the request permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE){
            if (grantResults.isNotEmpty()){
                for (result in grantResults){
                    if (result == AppCompatActivity.RESULT_OK){
                        Toast.makeText(requireContext(), "Permissions are Granted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

        companion object {
            private const val TAG = "HomeFragment"
            private const val LOCATION_REQUEST_CODE = 0
        }

        override fun onDestroy() {
            super.onDestroy()
            _binding = null
        }
}