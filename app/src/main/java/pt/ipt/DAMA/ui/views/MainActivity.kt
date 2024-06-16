package pt.ipt.DAMA.ui.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import pt.ipt.DAMA.R
import pt.ipt.DAMA.hardware.CameraManager
import pt.ipt.DAMA.hardware.GpsManager
import pt.ipt.DAMA.retrofit.MyCookieJar

class MainActivity : ComponentActivity() {
    /*
    * Managers for hardware components
    */
    private lateinit var gpsManager: GpsManager
    private lateinit var cameraManager: CameraManager
    // UI components
    private lateinit var signInButton: Button
    private lateinit var signUpButton: Button
    private lateinit var skipButton: Button

    /*
    * Activity result launcher for requesting multiple permissions
    */
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allPermissionsGranted = true
            permissions.entries.forEach {
                if (!it.value) {
                    allPermissionsGranted = false
                }
            }

            if (allPermissionsGranted) {
                // All permissions are granted
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                // Some permissions are denied
                showPermissionDeniedDialog()
            }
        }

    /*
    * Initializes the activity
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if user is already logged in
        val cookieJar = MyCookieJar(this)
        if (cookieJar.isUserLoggedIn()) {
            // User is logged in, redirect to the main content
            val intent = Intent(this, ArActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        gpsManager = GpsManager(this)
        cameraManager = CameraManager(this)

        if (allPermissionsGranted()) {
            // All permissions are already granted
            // display a toast message
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissions()
        }

        // Set up button click listeners
        signInButton = findViewById(R.id.sign_in_button)
        signUpButton = findViewById(R.id.sign_up_button)
        skipButton = findViewById(R.id.skip)

        // Set up sign in button click listener
        signInButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Set up sign up button click listener
        signUpButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Set up skip button click listener
        skipButton.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }
    }

    /*
    * function to check if all required permissions are granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /*
    * function to request permissions
     */
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    /*
    * function to show permission denied dialog
     */
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("This app requires Camera and Location permissions to function properly. Please enable them in the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /*
    *companion object
    *  that defines the required permissions for your app
    */
    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
