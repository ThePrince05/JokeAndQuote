package com.project.jokeandquote.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.project.jokeandquote.R
import com.project.jokeandquote.service.TalentDetailsDao
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomBar: ChipNavigationBar
    var isSetupComplete = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomBar)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = systemBars.bottom
            }
            insets
        }
        // Handle back gesture
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        bottomBar = findViewById(R.id.bottomBar)

        // Launch a coroutine to check TalentDetails
        lifecycleScope.launch {
            val dao = TalentDetailsDao(applicationContext)
            val hasDetails = dao.hasTalentDetails()

            if (!hasDetails) {
                // Setup not complete: show dialog and redirect to Settings
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("Setup Required")
                    .setMessage("Please set up your details in Settings before creating a quotation or invoice.")
                    .setCancelable(false)
                    .setPositiveButton("Go to Settings") { dialog, _ ->
                        dialog.dismiss()
                        bottomBar.setItemSelected(R.id.Settings, true)
                        navController.navigate(R.id.settingsFragment)
                    }
                    .show()
            } else {
                // Setup is complete
                isSetupComplete = true
                bottomBar.setItemSelected(R.id.Quotation, true)
                navController.navigate(R.id.quotationsFragment)
            }
        }

        bottomBar.setOnItemSelectedListener { id ->
            if (!isSetupComplete && id != R.id.Settings) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Setup Required")
                    .setMessage("Please complete setup in Settings before accessing other sections.")
                    .setCancelable(false)
                    .setPositiveButton("Go to Settings") { dialog, _ ->
                        dialog.dismiss()
                        bottomBar.setItemSelected(R.id.Settings, true)
                        navController.navigate(R.id.settingsFragment)
                    }
                    .show()
                return@setOnItemSelectedListener
            }

            when (id) {
                R.id.Quotation -> navController.navigate(R.id.quotationsFragment)
                R.id.Invoice -> navController.navigate(R.id.invoiceFragment)
                R.id.History -> navController.navigate(R.id.historyFragment)
                R.id.Settings -> navController.navigate(R.id.settingsFragment)
            }
        }
    }

    // Check for write Permissions
    private val STORAGE_PERMISSION_CODE = 1001

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                onPermissionGrantedCallback?.invoke()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private var onPermissionGrantedCallback: (() -> Unit)? = null

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit the application?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity() // Closes all activities
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}