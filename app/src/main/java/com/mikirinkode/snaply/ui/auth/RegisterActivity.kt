package com.mikirinkode.snaply.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.mikirinkode.snaply.databinding.ActivityRegisterBinding
import com.mikirinkode.snaply.ui.main.MainActivity
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

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.isLoading.observe(this) { showLoading(it) }

        binding.apply {
            btnRegister.setOnClickListener {
                val name = edtRegisterName.text.toString().trim()
                val email = edtRegisterEmail.text.toString().trim()
                val password = edtRegisterPassword.text.toString().trim()
                val passwordConf = edtRegisterPasswordConf.text.toString().trim()
                when {
                    TextUtils.isEmpty(name) -> edtRegisterName.error =
                        "Name cannot be empty"
                    TextUtils.isEmpty(email) -> edtRegisterEmail.error =
                        "email cannot be empty"
                    TextUtils.isEmpty(password) -> edtRegisterPassword.error =
                        "Password cannot be empty"
                    passwordConf != password -> edtRegisterPasswordConf.error =
                        "Password didn't match"
                    else -> {
                        viewModel.registerNewUser(name, email, password)

                        viewModel.isError.observe(this@RegisterActivity) { isError ->
                            if (isError) {
                                viewModel.responseMessage.observe(this@RegisterActivity){
                                    if(it != null) Toast.makeText(this@RegisterActivity, it, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                viewModel.responseMessage.observe(this@RegisterActivity){
                                    if(it != null) Toast.makeText(this@RegisterActivity, it, Toast.LENGTH_SHORT).show()
                                }

                                preferences.setValues(Preferences.USER_EMAIL, email)
                                preferences.setValues(Preferences.USER_PASSWORD, password)

                                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                                finish()
                            }
                        }
                    }
                }
            }

            btnLogin.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }
        }
    }

    private fun showLoading(state: Boolean) {
        binding.apply {
            if (state) loadingIndicator.visibility = View.VISIBLE else loadingIndicator.visibility = View.GONE
            if (state) loading.visibility = View.VISIBLE else loading.visibility = View.GONE
        }
    }
}