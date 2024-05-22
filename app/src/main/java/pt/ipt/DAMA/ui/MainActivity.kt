package pt.ipt.DAMA.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.DAMA.R
import pt.ipt.DAMA.hardware.CameraManager
import pt.ipt.DAMA.hardware.GpsManager


class MainActivity : AppCompatActivity() {
    private lateinit var gpsManager: GpsManager
    private lateinit var cameraManager: CameraManager
    private lateinit var btnLocation: Button
    private lateinit var btnCamera: Button
    private lateinit var txt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gpsManager = GpsManager(this)
        gpsManager.getLocation()
        cameraManager = CameraManager(this)
        btnLocation = findViewById(R.id.getLocation)
        btnCamera = findViewById(R.id.openCamera)
        txt = findViewById(R.id.text)

        btnLocation.setOnClickListener {
            var currentLocation = gpsManager.getCurrentLocation()

            if (currentLocation != null) {
                txt.text = "Latitude: ${currentLocation.latitude}\n " +
                        "Longitude: ${currentLocation.longitude}\n"+
                        "Altitude: ${currentLocation.altitude}"
            }else{
                txt.text = "Location not found"
            }
        }

        btnCamera.setOnClickListener {
            cameraManager.openCamera()
        }
    }

}

