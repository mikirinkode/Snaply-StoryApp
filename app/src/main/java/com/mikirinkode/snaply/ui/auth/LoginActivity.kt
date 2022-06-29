package com.mikirinkode.snaply.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import com.mikirinkode.snaply.databinding.ActivityLoginBinding
import com.mikirinkode.snaply.ui.main.MainActivity
import com.mikirinkode.snaply.utils.Preferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }


    @Inject
    lateinit var preferences: Preferences

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // check preference for dark mode
        val isDark = preferences.getBooleanValues(Preferences.DARK_MODE_PREF)
        if (isDark == true) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // check prefs for email and pass
        val userEmail = preferences.getStringValues(Preferences.USER_EMAIL)
        val userPassword = preferences.getStringValues(Preferences.USER_PASSWORD)
        if (!userEmail.isNullOrEmpty() && !userPassword.isNullOrEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        viewModel.isLoading.observe(this) { showLoading(it) }

        binding.apply {
            btnLogin.setOnClickListener {
                val email = edtLoginEmail.text.toString().trim()
                val password = edtLoginPassword.text.toString().trim()

                when {
                    TextUtils.isEmpty(email) -> edtLoginEmail.error =
                        "email cannot be empty"
                    TextUtils.isEmpty(password) -> edtLoginPassword.error =
                        "Password cannot be empty"
                    else -> {
                        loginUser(email, password)
                    }
                }
            }

            btnRegisterNow.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        viewModel.loginUser(email, password)

        viewModel.responseMessage.observe(this@LoginActivity) {
            if (it != null) Toast.makeText(
                this@LoginActivity,
                it,
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.isError.observe(this@LoginActivity) { isError ->

            if (!isError) {
                preferences.setValues(Preferences.USER_EMAIL, email)
                preferences.setValues(Preferences.USER_PASSWORD, password)
                startActivity(
                    Intent(
                        this@LoginActivity,
                        MainActivity::class.java
                    )
                )
                finish()
            }
        }
    }

    private fun showLoading(state: Boolean) {
        binding.apply {
            if (state) loading.visibility = View.VISIBLE else loading.visibility = View.GONE
        }
    }
}