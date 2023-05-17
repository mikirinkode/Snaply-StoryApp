package com.mikirinkode.snaply.ui.main.maps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.data.Result
import com.mikirinkode.snaply.data.model.StoryEntity
import com.mikirinkode.snaply.databinding.FragmentHomeBinding
import com.mikirinkode.snaply.databinding.FragmentMapsBinding
import com.mikirinkode.snaply.ui.detail.DetailActivity
import com.mikirinkode.snaply.ui.maps.MapsActivity
import com.mikirinkode.snaply.utils.Preferences
import com.mikirinkode.snaply.viewmodel.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!


    @Inject
    lateinit var preferences: Preferences
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView

    private val storyViewModel: StoryViewModel by viewModels()
    private var storyList: ArrayList<StoryEntity> = arrayListOf()

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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)

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
            if (cardStoryImage.visibility == View.VISIBLE){
                cardStoryImage.visibility = View.GONE
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.e(TAG, "onMarkerClick")
        if (storyList.isNotEmpty()) {
            Log.e(TAG, "storyList is not empty")

            val selectedStory = storyList.find { it.id == marker.title }
            Log.e(TAG, "selectedStory is ${selectedStory}")
            Log.e(TAG, "selectedStory id is ${selectedStory?.id}")
            Log.e(TAG, "marker id is ${marker?.title}")

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
        } else {

            Log.e(TAG, "storyList is empty")
        }
        return true
    }

    private fun observeStoryList() {
        val userToken = preferences.getStringValues(Preferences.USER_TOKEN)
        if (userToken != null) {
            storyViewModel.getStoryWithLocationList(userToken)
                .observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Result.Success -> {
                            binding.loading.visibility = View.GONE
                            if (result.data.isNotEmpty()) {
                                Log.e(TAG, result.data.size.toString())
                                Log.e(TAG, result.data.size.toString())
                                Log.e(TAG, result.data.toString())
                                createAllStoryMarkers(result.data)
                                storyList.addAll(result.data)
                            }
                        }
                        is Result.Loading -> {
                            binding.loading.visibility = View.VISIBLE
                        }
                        is Result.Error -> {
                            binding.loading.visibility = View.GONE
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