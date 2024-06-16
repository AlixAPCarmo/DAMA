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

/**
 *  Manages permissions for the application, handling requests and results.
 */
class PermissionsManager(
    private val context: Context,
    private val activityResultLauncher: ActivityResultLauncher<Array<String>>) {
    companion object {
        // List of permissions required by the application
        val REQUIRED_PERMISSIONS = mutableListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    /**
     * Check if all required permissions are granted
     */
    fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request permissions if not all have been granted
     */
    fun requestPermissions() {
        if (!allPermissionsGranted()) {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            Toast.makeText(context,
                context.getString(R.string.all_permissions_granted), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Handle the result of the permission request
     */
    fun handlePermissionsResult(permissions: Map<String, Boolean>) {
        var allPermissionsGranted = true
        permissions.entries.forEach {
            if (!it.value) allPermissionsGranted = false
        }

        if (allPermissionsGranted) {
            Toast.makeText(context, context.getString(R.string.all_permissions_granted), Toast.LENGTH_SHORT).show()
        } else {
            showPermissionDeniedDialog()
        }
    }

    /**
     * Shows a dialog informing the user that permissions are needed for proper functionality.
     */
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.permissions_required))
            .setMessage(context.getString(R.string.this_app_requires_camera_and_location_permissions_to_function_properly_please_enable_them_in_the_app_settings))
            .setPositiveButton(context.getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}