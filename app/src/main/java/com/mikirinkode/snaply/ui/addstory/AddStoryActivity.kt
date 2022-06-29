package com.mikirinkode.snaply.ui.addstory

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mikirinkode.snaply.databinding.ActivityAddStoryBinding
import com.mikirinkode.snaply.ui.main.MainActivity
import com.mikirinkode.snaply.ui.main.MainViewModel
import com.mikirinkode.snaply.utils.Preferences
import com.mikirinkode.snaply.utils.reduceFileImage
import com.mikirinkode.snaply.utils.rotateBitmap
import com.mikirinkode.snaply.utils.uriToFile
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

    private val mainViewModel: MainViewModel by viewModels()

    private var getFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // check if permission not granted, then request for permission else show dialog
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            showPictureDialog()
        }

        binding.apply {

            btnBack.setOnClickListener { onBackPressed() }

            btnUpload.setOnClickListener { uploadImage() }

            btnAddPhoto.setOnClickListener {
                if (!allPermissionsGranted()) {
                    ActivityCompat.requestPermissions(
                        this@AddStoryActivity,
                        REQUIRED_PERMISSIONS,
                        REQUEST_CODE_PERMISSIONS
                    )
                } else {
                    showPictureDialog()
                }
            }
        }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Pilih Aksi:")
        val pictureDialogItem = arrayOf(
            "Ambil Gambar dari Galeri",
            "Ambil dengan Kamera"
        )
        pictureDialog.setItems(pictureDialogItem) { _, which ->
            when (which) {
                0 -> openGallery()
                1 -> openCamera()
            }
        }
        pictureDialog.show()
    }

    private fun openGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this)
            getFile = myFile
            binding.ivStoryPhoto.setImageURI(selectedImg)
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
            binding.ivStoryPhoto.setImageBitmap(result)
        }
    }

    private fun uploadImage() {
        binding.apply {
            val token = preferences.getStringValues(Preferences.USER_TOKEN)

            val inputDesc = edtStoryCaption.text.toString().trim()
            if (inputDesc.isEmpty()) {
                edtStoryCaption.error = "Description is empty"
            }

            if (getFile != null && inputDesc.isNotEmpty()) {
                val file = reduceFileImage(getFile as File)

                val description = inputDesc.toRequestBody("text/plain".toMediaType())
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )

                if (token != null) {
                    mainViewModel.addNewStory(token, imageMultipart, description)

                    mainViewModel.responseMessage.observe(this@AddStoryActivity) {
                        if (it != null) Toast.makeText(
                            this@AddStoryActivity,
                            it,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    mainViewModel.isError.observe(this@AddStoryActivity) { isError ->
                        if (!isError) {
                            startActivity(Intent(this@AddStoryActivity, MainActivity::class.java))
                        }
                    }
                }
            } else {
                Toast.makeText(this@AddStoryActivity, "Please select an image.", Toast.LENGTH_SHORT)
                    .show()
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
                    "Tidak mendapatkan permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}