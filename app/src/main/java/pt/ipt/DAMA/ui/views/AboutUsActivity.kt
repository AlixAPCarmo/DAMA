package pt.ipt.DAMA.ui.views

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.DAMA.R

class AboutUsActivity: AppCompatActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lock the screen orientation to portrait mode for this activity
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // Set the content view to the "About Us" layout
        setContentView(R.layout.activity_aboutus)
        // Setup the back button to navigate back to the Main Activity
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
