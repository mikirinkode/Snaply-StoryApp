package com.mikirinkode.snaply.ui.main.maps

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.carousel.CarouselLayoutManager

import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.data.Result
import com.mikirinkode.snaply.data.model.StoryEntity
import com.mikirinkode.snaply.databinding.FragmentMapsBinding
import com.mikirinkode.snaply.ui.detail.DetailActivity
import com.mikirinkode.snaply.utils.LinearLatLngInterpolator
import com.mikirinkode.snaply.utils.Preferences
import com.mikirinkode.snaply.viewmodel.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapClickListener{

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!


    @Inject
    lateinit var preferences: Preferences
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView

    private val storyViewModel: StoryViewModel by viewModels()
    private var storyList: ArrayList<StoryEntity> = arrayListOf()

    private val carouselAdapter: StoryAdapter by lazy {
        StoryAdapter(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        actionClick()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun initRecyclerView() {
        binding.apply {
            carouselRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            carouselRecyclerView.adapter = carouselAdapter

            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(carouselRecyclerView)

            carouselRecyclerView.addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)

                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            val position =
                                (carouselRecyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()


                            if (position >= 0) {
                                val item = carouselAdapter.getStoryByPosition(position)
                                navigateToLocation(LatLng(item.lat, item.lon))
                            } else {
                                val item = carouselAdapter.getStoryByPosition(0)
                                navigateToLocation(LatLng(item.lat, item.lon))
                            }
                        }
                    }
                })
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)
        try {
            // check preference for dark mode
            val isDark = preferences.getBooleanValues(Preferences.DARK_MODE_PREF)
            if (isDark) {
                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_night)
                )
            } else {
                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_light)
                )
            }
        } catch (e: Exception) {
            Log.e("SelectLocation", "${e.message}")
        }
        val initialLocation = LatLng(-4.375726916664182, 117.53723749844212)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialLocation))

        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = true


        observeStoryList()
    }

    override fun onMapClick(p0: LatLng) {
        binding.apply {
            if (cardStoryImage.visibility == View.VISIBLE) {
                cardStoryImage.visibility = View.GONE
            }
            if (errorMessage.visibility == View.VISIBLE) {
                errorMessage.visibility = View.GONE
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (storyList.isNotEmpty()) {

            val selectedStory = storyList.find { it.id == marker.title }

            if (selectedStory != null) {
                binding.cardStoryImage.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(selectedStory.photoUrl)
                    .into(binding.ivStoryImage)


                binding.cardStoryImage.setOnClickListener {
                    val intent = Intent(requireContext(), DetailActivity::class.java)
                    intent.putExtra(DetailActivity.EXTRA_STORY, selectedStory)

                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            requireActivity(),
                            Pair(binding.ivStoryImage, getString(R.string.story_image)),
                        )
                    startActivity(intent, optionsCompat.toBundle())
                }
            } else {
                binding.cardStoryImage.visibility = View.GONE
            }
        }

        return true
    }

    private fun observeStoryList() {

        storyViewModel.getStoryWithLocationList()
            .observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Result.Success -> {
                        binding.loading.visibility = View.GONE
                        if (result.data.isNotEmpty()) {
                            binding.errorMessage.visibility = View.GONE
                            createAllStoryMarkers(result.data)
                            storyList.addAll(result.data)
                            carouselAdapter.setData(result.data)
                        } else {
                            showNoDataDialog()
                        }
                    }
                    is Result.Loading -> {
                        binding.loading.visibility = View.VISIBLE
                    }
                    is Result.Error -> {
                        binding.loading.visibility = View.GONE
                        if (result.error.contains("Unable to resolve host")) {
                            showNoInternetConnectionDialog()
                        } else {
                            Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
    }

    private fun createAllStoryMarkers(list: List<StoryEntity>) {
        for (story in list) {
            val marker = createMarker(LatLng(story.lat, story.lon), story.id, story.description)
            mMap.addMarker(marker)
        }
    }

    private fun createMarker(latLng: LatLng, title: String, snippet: String): MarkerOptions {
        return MarkerOptions().position(latLng).title(title).snippet(snippet)
    }

    private fun showNoInternetConnectionDialog() {
        binding.apply {
            errorMessage.visibility = View.VISIBLE
            icErrorMsg.visibility = View.VISIBLE
        }
    }

    private fun showNoDataDialog() {
        binding.apply {
            errorMessage.visibility = View.VISIBLE
            icErrorMsg.visibility = View.GONE
            tvErrorTitle.text = getString(R.string.no_data)
            tvErrorDesc.text = getString(R.string.txt_empty_story_with_location)
        }
    }

    private fun navigateToLocation(latLng: LatLng, zoom: Float = 14.0f) {
        val tiltAngle = 45f // Set the desired tilt angle in degrees

        val cameraPosition = CameraPosition.Builder()
            .target(latLng) // Keep the current map center as the target
            .zoom(zoom) // Keep the current zoom level
            .bearing(mMap.cameraPosition.bearing) // Keep the current bearing angle
            .tilt(tiltAngle) // Set the desired tilt angle
            .build()

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun actionClick() {
        binding.apply {
            btnRetry.setOnClickListener {
                observeStoryList()
            }
        }
    }

    companion object {
        private const val TAG = "MapsFragment"
    }
}