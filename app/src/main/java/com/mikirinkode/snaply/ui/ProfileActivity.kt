package com.mikirinkode.snaply.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.databinding.ActivityProfileBinding
import com.mikirinkode.snaply.ui.auth.LoginActivity
import com.mikirinkode.snaply.utils.Preferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        binding.apply {

            val isDark = preferences.getBooleanValues(Preferences.DARK_MODE_PREF)
            switchDarkMode.isChecked = isDark == true

            switchDarkMode.setOnCheckedChangeListener { _, checked ->
                Log.e("ProfileDarkCheckValue:", checked.toString())
                preferences.setValues(Preferences.DARK_MODE_PREF, checked)
                if (checked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }

            switchDarkMode.setOnClickListener {
                Log.e("ProfileDarkActivated:", it.isActivated.toString())
                Log.e("ProfileDarkChecked:", switchDarkMode.isChecked.toString())
                if (it.isActivated){

                }

                if (switchDarkMode.isChecked){

                }
            }

            btnLogout.setOnClickListener {
                preferences.setValues(Preferences.USER_ID, "")
                preferences.setValues(Preferences.USER_NAME, "")
                preferences.setValues(Preferences.USER_TOKEN, "")
                preferences.setValues(Preferences.USER_EMAIL, "")
                preferences.setValues(Preferences.USER_PASSWORD, "")
                startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
            }
        }
    }
}