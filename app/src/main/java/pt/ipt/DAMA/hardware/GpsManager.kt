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

/**
 * Class to manage GPS services for obtaining location in an Android application
 */
class GpsManager(private val context: Context) : LocationListener {

    // LocationManager to interact with location services
    private var locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    // Define a request code for location permission
    private val locationPermissionCode = 2
    // Variable to store the current location
    private var currentLocation: Location? = null
    // Flag to check if GPS is initialized
    private var inicialized = false

    /**
     * Configure GPS services to obtain the current location
     */
    fun getLocation() {
        // Check if FINE LOCATION permission is not granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request FINE LOCATION permission
            (context as AppCompatActivity).requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        } else {
            // Permission has been granted, start getting location updates
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5f, this)
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else {
                // Notify user if GPS is disabled
                Toast.makeText(context, "GPS is disabled", Toast.LENGTH_SHORT).show()
            }
        }
        inicialized = true
    }

    /**
     * Called when the location has changed
     */
    override fun onLocationChanged(location: Location) {
        // save current position
        currentLocation = location
    }

    /**
     * Return the current location, initializes GPS if not already done
     */
    fun getCurrentLocation(): Location? {
        if (!inicialized) {
            getLocation()
        }
        return currentLocation
    }
}
