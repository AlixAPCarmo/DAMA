package pt.ipt.DAMA.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import pt.ipt.DAMA.R

class PermissionsManager(
    private val context: Context,
    private val activityResultLauncher: ActivityResultLauncher<Array<String>>) {
    companion object {
        val REQUIRED_PERMISSIONS = mutableListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions() {
        if (!allPermissionsGranted()) {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            Toast.makeText(context, "All permissions granted", Toast.LENGTH_SHORT).show()
        }
    }

    fun handlePermissionsResult(permissions: Map<String, Boolean>) {
        var allPermissionsGranted = true
        permissions.entries.forEach {
            if (!it.value) allPermissionsGranted = false
        }

        if (allPermissionsGranted) {
            Toast.makeText(context, "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            showPermissionDeniedDialog()
        }
    }

    /*
    * function to show permission denied dialog
     */
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(context)
            .setTitle("Permissions Required")
            .setMessage("This app requires Camera and Location permissions to function properly. Please enable them in the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}