package com.mikirinkode.snaply.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.data.Result
import com.mikirinkode.snaply.databinding.ActivityLoginBinding
import com.mikirinkode.snaply.ui.main.MainActivity
import com.mikirinkode.snaply.viewmodel.UserViewModel
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

    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        playAnimation()

        // check preference for dark mode
        val isDark = preferences.getBooleanValues(Preferences.DARK_MODE_PREF)
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // check prefs for user token
        val userToken = preferences.getStringValues(Preferences.USER_TOKEN)
        if (!userToken.isNullOrEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.apply {
            btnLogin.setOnClickListener {
                errorMessage.visibility = View.GONE

                val email = edtLoginEmail.text.toString().trim()
                val password = edtLoginPassword.text.toString().trim()

                when {
                    TextUtils.isEmpty(email) -> edtLoginEmail.error =
                        getString(R.string.txt_edt_error_empty_email)
                    TextUtils.isEmpty(password) -> edtLoginPassword.error =
                        getString(R.string.txt_edt_error_empty_password)
                    else -> {
                        // close keyboard
                        edtLoginEmail.onEditorAction(EditorInfo.IME_ACTION_DONE)

                        loginUser(email, password)
                    }
                }
            }

            btnRegisterNow.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                errorMessage.visibility = View.GONE
            }

            btnRetry.setOnClickListener {
                errorMessage.visibility = View.GONE
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        viewModel.loginUser(email, password).observe(this) { result ->
            when (result){
                is Result.Success -> {
                    showLoading(false)
                    startActivity(
                        Intent(
                            this@LoginActivity,
                            MainActivity::class.java
                        )
                    )
                    finish()
                }
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Error -> {
                    showLoading(false)
                    Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
                    showErrorMessage(result.error)
                }
            }
        }
    }


    private fun showErrorMessage(message: String) {
        binding.apply {
            if (message.contains("Unable to resolve host")) {
                tvErrorDesc.text = getString(R.string.txt_no_internet_desc)
            } else {
                tvErrorDesc.text = message
            }
            errorMessage.visibility = View.VISIBLE
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
        }
    }

    private fun playAnimation() {
        binding.apply {
            val appName = ObjectAnimator.ofFloat(tvAppName, View.ALPHA, 1f).setDuration(500)
            val loginDesc = ObjectAnimator.ofFloat(tvLoginDesc, View.ALPHA, 1f).setDuration(500)
            val email = ObjectAnimator.ofFloat(tilLoginEmail, View.ALPHA, 1f).setDuration(1000)
            val password =
                ObjectAnimator.ofFloat(tilLoginPassword, View.ALPHA, 1f).setDuration(1000)
            val btnLogin = ObjectAnimator.ofFloat(btnLogin, View.ALPHA, 1f).setDuration(1000)
            val tvNotHaveAcc =
                ObjectAnimator.ofFloat(tvNotHaveAcc, View.ALPHA, 1f).setDuration(1000)
            val btnRegister =
                ObjectAnimator.ofFloat(btnRegisterNow, View.ALPHA, 1f).setDuration(1000)

            val together = AnimatorSet().apply {
                playTogether(email, password, tvNotHaveAcc, btnLogin, btnRegister)
            }

            AnimatorSet().apply {
                playSequentially(together, appName, loginDesc)
                start()
            }
        }

    }
}