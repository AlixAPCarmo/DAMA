package pt.ipt.DAMA.hardware

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class GpsManager(private val context: Context) : LocationListener {

    private var locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val locationPermissionCode = 2
    private var currentLocation: Location? = null
    private var inicialized = false

    fun getLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            (context as AppCompatActivity).requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        } else {
            // Permission already granted
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5f, this)
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else {
                Toast.makeText(context, "GPS is disabled", Toast.LENGTH_SHORT).show()
            }
        }
        inicialized = true
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
        Toast.makeText(context, "Latitude: ${location.latitude}, Longitude: ${location.longitude}", Toast.LENGTH_LONG).show()
    }

    fun getCurrentLocation(): Location? {
        if (!inicialized) {
            getLocation()
        }
        return currentLocation
    }
}
