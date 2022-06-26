package com.mikirinkode.snaply

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mikirinkode.snaply.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            btnLogin.setOnClickListener {

            }
        }
    }
}