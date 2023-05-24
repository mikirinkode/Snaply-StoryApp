package com.mikirinkode.snaply.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.data.Result
import com.mikirinkode.snaply.databinding.ActivityRegisterBinding
import com.mikirinkode.snaply.viewmodel.UserViewModel
import com.mikirinkode.snaply.utils.Preferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var preferences: Preferences

    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        playAnimation()

        binding.apply {
            btnRegister.setOnClickListener {
                errorMessage.visibility = View.GONE

                val name = edtRegisterName.text.toString().trim()
                val email = edtRegisterEmail.text.toString().trim()
                val password = edtRegisterPassword.text.toString().trim()
                val passwordConf = edtRegisterPasswordConf.text.toString().trim()
                when {
                    TextUtils.isEmpty(name) -> edtRegisterName.error =
                        getString(R.string.txt_edt_error_empty_name)
                    TextUtils.isEmpty(email) -> edtRegisterEmail.error =
                        getString(R.string.txt_edt_error_empty_email)
                    TextUtils.isEmpty(password) -> edtRegisterPassword.error =
                        getString(R.string.txt_edt_error_empty_password)
                    passwordConf != password -> edtRegisterPasswordConf.error =
                        getString(R.string.txt_edt_error_password_not_match)
                    else -> {
                        // close keyboard
                        edtRegisterName.onEditorAction(EditorInfo.IME_ACTION_DONE)

                        // register user account
                        viewModel.registerNewUser(name, email, password)
                            .observe(this@RegisterActivity) { result ->
                                when (result) {
                                    is Result.Success -> {
                                        showLoading(false)
                                        Toast.makeText(
                                            this@RegisterActivity,
                                            result.data,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        preferences.setValues(Preferences.USER_EMAIL, email)

                                        startActivity(
                                            Intent(
                                                this@RegisterActivity,
                                                LoginActivity::class.java
                                            )
                                        )
                                        finishAffinity()
                                    }
                                    is Result.Error -> {
                                        showLoading(false)
                                        showErrorMessage(result.error)
                                        Toast.makeText(
                                            this@RegisterActivity,
                                            result.error,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    is Result.Loading -> {
                                        showLoading(true)
                                    }
                                }
                            }
                    }
                }
            }

            btnLogin.setOnClickListener {
                errorMessage.visibility = View.GONE
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }

            btnRetry.setOnClickListener {
                errorMessage.visibility = View.GONE
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
            if (state) loading.visibility = View.VISIBLE else loading.visibility = View.GONE
        }
    }

    private fun playAnimation() {
        binding.apply {
            val appName = ObjectAnimator.ofFloat(tvAppName, View.ALPHA, 1f).setDuration(500)
            val registerDesc =
                ObjectAnimator.ofFloat(tvRegisterDesc, View.ALPHA, 1f).setDuration(500)
            val fullName = ObjectAnimator.ofFloat(tilRegisterName, View.ALPHA, 1f).setDuration(1000)
            val email = ObjectAnimator.ofFloat(tilRegisterEmail, View.ALPHA, 1f).setDuration(1000)
            val password =
                ObjectAnimator.ofFloat(tilRegisterPassword, View.ALPHA, 1f).setDuration(1000)
            val passwordConf =
                ObjectAnimator.ofFloat(tilRegisterPasswordConf, View.ALPHA, 1f).setDuration(1000)
            val btnLogin = ObjectAnimator.ofFloat(btnLogin, View.ALPHA, 1f).setDuration(1000)
            val tvHaveAcc = ObjectAnimator.ofFloat(tvHaveAcc, View.ALPHA, 1f).setDuration(1000)
            val btnRegister = ObjectAnimator.ofFloat(btnRegister, View.ALPHA, 1f).setDuration(1000)

            val together = AnimatorSet().apply {
                playTogether(
                    fullName,
                    email,
                    password,
                    passwordConf,
                    tvHaveAcc,
                    btnRegister,
                    btnLogin
                )
            }

            AnimatorSet().apply {
                playSequentially(together, appName, registerDesc)
                start()
            }
        }

    }
}