package pt.ipt.DAMA.hardware

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import pt.ipt.DAMA.R

class CameraManager(private val context: Context) {

    private val cameraPermissionCode = 1

    fun openCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            (context as AppCompatActivity).requestPermissions(arrayOf(Manifest.permission.CAMERA), cameraPermissionCode)
        } else {
            // Permission already granted
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            context.startActivity(cameraIntent)
        }
    }
}
