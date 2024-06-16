package pt.ipt.DAMA.hardware

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Class to manage camera operations in an Android context
 */
class CameraManager(private val context: Context) {

    // Define a request code for camera permission
    private val cameraPermissionCode = 1

    /**
     *Function to open the device camera
     */
    fun openCamera() {
        // Check if the CAMERA permission is not granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request CAMERA permission
            (context as AppCompatActivity).requestPermissions(arrayOf(Manifest.permission.CAMERA), cameraPermissionCode)
        } else {
            // Permission has been granted, start the camera activity
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            context.startActivity(cameraIntent)
        }
    }
}
