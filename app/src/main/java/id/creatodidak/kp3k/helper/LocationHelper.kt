package id.creatodidak.kp3k.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority

class LocationHelper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val callback: (Location?) -> Unit
) {

    private var locationCallback: LocationCallback? = null
    private var bestLocation: Location? = null
    private var isRequesting = false

    fun requestLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            callback(null)
            return
        }

        bestLocation = null
        isRequesting = true

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(500L)
            .setMaxUpdateDelayMillis(2000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location.accuracy <= 1f) { // misal <= 5 meter
                        bestLocation = location
                        callback(bestLocation)
                        stop() // akurasi sudah bagus, baru stop
                        return
                    } else {
                        // kasih update sementara
                        callback(location)
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            (context as Activity).mainLooper
        )
    }


    fun stop() {
        if (isRequesting && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback!!)
            isRequesting = false
        }
    }
}
