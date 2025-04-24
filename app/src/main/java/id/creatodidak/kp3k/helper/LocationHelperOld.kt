package id.creatodidak.kp3k.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.*

class LocationHelperOld(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val callback: (Location?) -> Unit
) {
    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private var locationRequest: LocationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 10000L // interval reguler
    ).apply {
        setMinUpdateIntervalMillis(5000L) // interval tercepat
    }.build()


    private var locationCallback: LocationCallback? = null
    private var isRequesting = false

    fun requestLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            callback(null)
            return
        }

        if (isRequesting) return

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    if (location.accuracy <= 5f) {
                        callback(location)
                        stop()
                    } else {
                        callback(location)
                    }
                } else {
                    callback(null)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            null
        )

        isRequesting = true
    }

    fun stop() {
        if (isRequesting && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback!!)
            isRequesting = false
        }
    }
}
