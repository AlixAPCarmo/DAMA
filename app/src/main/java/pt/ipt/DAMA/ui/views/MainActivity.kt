package pt.ipt.DAMA.ui.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import pt.ipt.DAMA.R
import pt.ipt.DAMA.hardware.CameraManager
import pt.ipt.DAMA.hardware.GpsManager
import pt.ipt.DAMA.retrofit.MyCookieJar
import pt.ipt.DAMA.utils.PermissionsManager

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
    private lateinit var aboutButton: ImageButton
    private lateinit var permissionManager: PermissionsManager

    /*
    * Initializes the activity
    */
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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

        // Initialize permission manager
        permissionManager = PermissionsManager(this, registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionManager.handlePermissionsResult(permissions)
        })

        if (!permissionManager.allPermissionsGranted()) {
            permissionManager.requestPermissions()
        }

        // Set up button click listeners
        signInButton = findViewById(R.id.sign_in_button)
        signUpButton = findViewById(R.id.sign_up_button)
        skipButton = findViewById(R.id.skip)
        aboutButton = findViewById(R.id.about_button)

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
            val intent = Intent(this, ArActivity::class.java)
            startActivity(intent)
        }

        aboutButton.setOnClickListener {
            val intent = Intent(this, AboutUsActivity::class.java)
            startActivity(intent)
        }
    }
}
