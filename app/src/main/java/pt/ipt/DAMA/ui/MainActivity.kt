package pt.ipt.DAMA.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import pt.ipt.DAMA.R
import pt.ipt.DAMA.hardware.CameraManager
import pt.ipt.DAMA.hardware.GpsManager

class MainActivity : ComponentActivity() {
    /*
    * Managers for hardware components
    */
    private lateinit var gpsManager: GpsManager
    private lateinit var cameraManager: CameraManager

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
                // Initialize or start required functionality here
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

        gpsManager = GpsManager(this)
        cameraManager = CameraManager(this)

        if (allPermissionsGranted()) {
            // All permissions are already granted
            // Initialize or start required functionality here
        } else {
            requestPermissions()
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
