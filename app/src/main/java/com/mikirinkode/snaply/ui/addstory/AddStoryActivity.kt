package com.mikirinkode.snaply.ui.addstory

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.data.Result
import com.mikirinkode.snaply.databinding.ActivityAddStoryBinding
import com.mikirinkode.snaply.ui.main.MainActivity
import com.mikirinkode.snaply.viewmodel.StoryViewModel
import com.mikirinkode.snaply.utils.*
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AddStoryActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAddStoryBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var preferences: Preferences

    private val storyViewModel: StoryViewModel by viewModels()

    private var getFile: File? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        showDialog()

        binding.apply {

            btnBack.setOnClickListener { onBackPressed() }

            btnUpload.setOnClickListener { processUpload() }

            btnAddPhotoCenter.setOnClickListener { showDialog() }

            btnAddPhoto.setOnClickListener { showDialog() }

            switchEnableLocation.setOnCheckedChangeListener { _, checked ->
                val latitude = preferences.getStringValues(Preferences.SAVED_LATITUDE)
                val longitude = preferences.getStringValues(Preferences.SAVED_LONGITUDE)

                if (checked) {
                    layoutSelectLocation.visibility = View.VISIBLE
                    if (latitude.isNullOrEmpty() && longitude.isNullOrEmpty()) {
                        tvLocationLatLong.text = "You haven't select location before"
                    } else {
                        selectedLatitude = latitude?.toDouble()
                        selectedLongitude = longitude?.toDouble()

                        val location = "Your Address:" +
                                "\nLat: $latitude" +
                                "\nLong: $longitude"

                        tvLocationLatLong.text = location
                    }
                } else {
                    layoutSelectLocation.visibility = View.GONE
                }
            }
            btnSelectLocation.setOnClickListener {
                val intent = Intent(this@AddStoryActivity, SelectLocationActivity::class.java)
                startActivityForResult(intent, SELECT_LOCATION_CODE)
            }

            btnCopy.setOnClickListener {
                val text = "Your Address:" +
                        "\nLat: $selectedLatitude" +
                        "\nLong: $selectedLongitude"
                val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("text", text)
                clipboardManager.setPrimaryClip(clipData)

                Toast.makeText(this@AddStoryActivity, "Text copied to clipboard", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun showDialog() {
        // check if permission not granted, then request for permission else show picture dialog
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this@AddStoryActivity,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle(getString(R.string.choose_action))
            val pictureDialogItem = arrayOf(
                getString(R.string.from_gallery),
                getString(R.string.using_camera)
            )
            pictureDialog.setItems(pictureDialogItem) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            pictureDialog.show()
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this)
            getFile = myFile

            binding.apply {
                if (getFile == null) btnAddPhotoCenter.visibility =
                    View.VISIBLE else btnAddPhotoCenter.visibility = View.GONE
                ivStoryPhoto.setImageURI(selectedImg)
            }
        }
    }

    // Open CameraX
    private fun openCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    // resultForActivity to get returned file from CameraActivity
    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            getFile = myFile
            val result = rotateBitmap(BitmapFactory.decodeFile(myFile.path), isBackCamera)

            binding.apply {
                if (getFile == null) btnAddPhotoCenter.visibility =
                    View.VISIBLE else btnAddPhotoCenter.visibility = View.GONE
                ivStoryPhoto.setImageBitmap(result)
            }
        }
    }

    private fun processUpload() {
        binding.apply {
            val token = preferences.getStringValues(Preferences.USER_TOKEN)

            val inputDesc = edtStoryCaption.text.toString().trim()
            if (inputDesc.isEmpty()) {
                edtStoryCaption.error = getString(R.string.empty_desc)
            }

            if (inputDesc.isNotEmpty()) {
                if (getFile != null) {
                    val file = reduceFileImage(getFile as File)
                    val description = inputDesc
                    val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                        "photo",
                        file.name,
                        requestImageFile
                    )

                    if (token != null) {
                        if (switchEnableLocation.isChecked) {
                            if (selectedLatitude == null && selectedLongitude == null) {
                                Toast.makeText(
                                    this@AddStoryActivity,
                                    "Please select a location first if you want to enable location.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return
                            }
                        }
                        storyViewModel.addNewStory(token, imageMultipart, description, selectedLatitude, selectedLongitude)
                            .observe(this@AddStoryActivity) { result ->
                                when (result) {
                                    is Result.Success -> {
                                        Toast.makeText(
                                            this@AddStoryActivity,
                                            result.data.toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        loadingIndicator.visibility = View.GONE
                                        startActivity(
                                            Intent(
                                                this@AddStoryActivity,
                                                MainActivity::class.java
                                            )
                                        )
                                        finishAffinity()
                                    }
                                    is Result.Error -> {
                                        Toast.makeText(
                                            this@AddStoryActivity,
                                            result.error.toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        loadingIndicator.visibility = View.GONE
                                    }
                                    Result.Loading -> {
                                        loadingIndicator.visibility = View.VISIBLE
                                    }
                                }
                            }
                    }
                } else {
                    Toast.makeText(
                        this@AddStoryActivity,
                        getString(R.string.select_img),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

        }
    }

    // check permission for Camera
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    getString(R.string.permission_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_LOCATION_CODE) {
            if (resultCode == RESULT_OK) {
                val latitude = data?.getDoubleExtra(INTENT_LAT, 0.0)
                val longitude = data?.getDoubleExtra(INTENT_LONG, 0.0)
                val result = data?.getStringExtra(INTENT_ADDRESS) +
                        "\nLatitude: $latitude" +
                        "\nLongitude: $longitude"


                selectedLatitude = latitude
                selectedLongitude = longitude

                binding.tvLocationLatLong.text = result
            }
        }
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
        const val SELECT_LOCATION_CODE = 1011
        const val INTENT_LAT = "intent_lat_lat"
        const val INTENT_LONG = "intent_long"
        const val INTENT_ADDRESS = "intent_address"
    }
}