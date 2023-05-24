package com.mikirinkode.snaply.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikirinkode.snaply.R
import com.mikirinkode.snaply.databinding.ActivityMainBinding
import com.mikirinkode.snaply.ui.addstory.AddStoryActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_home)

        binding.apply {
            val bottomNavView: BottomNavigationView = bottomNavView
            bottomNavView.background = null
            bottomNavView.menu.getItem(1).isEnabled = false

            bottomNavView.setupWithNavController(navController)

            fabAddStory.setOnClickListener {
                startActivity(Intent(this@MainActivity, AddStoryActivity::class.java))
            }
        }
    }

    override fun onBackPressed() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(getString(R.string.txt_dialog_exit_app))
        builder.setMessage(getString(R.string.txt_dialog_desc_exit_app))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.txt_dialog_sure)) { _, _ ->
                finish()
            }
            .setNegativeButton(getString(R.string.txt_dialog_no)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
}