package com.roamio.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.roamio.core.constants.CoreConstants
import com.roamio.core.util.LogUtil
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Resolves device location or returns a configured fallback city, with a place name.
 *
 * @param context Application context for location services and permission checks.
 * @param placeNameResolver Reverse-geocoder used to name the coordinates.
 * @author udit
 */
class LocationProvider(
    private val context: Context,
    private val placeNameResolver: PlaceNameResolver,
) {

    /**
     * Returns the current location when permission is granted, otherwise the fallback city.
     *
     * @return Resolved [GeoLocation], possibly marked as fallback.
     * @author udit
     */
    suspend fun getLocation(): GeoLocation {
        if (!hasLocationPermission()) {
            return namedLocation(
                latitude = CoreConstants.Location.FALLBACK_LATITUDE,
                longitude = CoreConstants.Location.FALLBACK_LONGITUDE,
                isFallback = true,
            )
        }
        val lastKnown = getLastKnownLocation()
        if (lastKnown != null) {
            return namedLocation(
                latitude = lastKnown.latitude,
                longitude = lastKnown.longitude,
                isFallback = false,
            )
        }
        val updated = withTimeoutOrNull(CoreConstants.Location.LOCATION_TIMEOUT_MILLIS) {
            requestSingleUpdate()
        }
        return if (updated != null) {
            namedLocation(
                latitude = updated.latitude,
                longitude = updated.longitude,
                isFallback = false,
            )
        } else {
            LogUtil.logInfo(CoreConstants.Logging.MSG_LOCATION_UNAVAILABLE)
            namedLocation(
                latitude = CoreConstants.Location.FALLBACK_LATITUDE,
                longitude = CoreConstants.Location.FALLBACK_LONGITUDE,
                isFallback = true,
            )
        }
    }

    /**
     * Checks whether fine or coarse location permission is granted.
     *
     * @return True when at least one location permission is granted.
     * @author udit
     */
    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private suspend fun namedLocation(
        latitude: Double,
        longitude: Double,
        isFallback: Boolean,
    ): GeoLocation {
        val resolved = runCatching { placeNameResolver.resolve(latitude, longitude) }.getOrNull()
        val fallbackName = if (isFallback) {
            CoreConstants.Location.FALLBACK_CITY_NAME
        } else {
            CoreConstants.Location.CURRENT_LOCATION_LABEL
        }
        return GeoLocation(
            latitude = latitude,
            longitude = longitude,
            isFallback = isFallback,
            placeName = resolved?.placeName.orEmpty().ifBlank { fallbackName },
            countryCode = resolved?.countryCode.orEmpty(),
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(
            LocationManager.FUSED_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
        var best: Location? = null
        for (provider in providers) {
            if (!manager.isProviderEnabled(provider)) continue
            val location = manager.getLastKnownLocation(provider) ?: continue
            if (best == null || location.accuracy < best.accuracy) {
                best = location
            }
        }
        return best
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleUpdate(): Location? {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = when {
            manager.isProviderEnabled(LocationManager.FUSED_PROVIDER) ->
                LocationManager.FUSED_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                LocationManager.NETWORK_PROVIDER
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                LocationManager.GPS_PROVIDER
            else -> return null
        }
        return suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    manager.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                override fun onProviderEnabled(provider: String) = Unit

                override fun onProviderDisabled(provider: String) {
                    manager.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
            continuation.invokeOnCancellation {
                manager.removeUpdates(listener)
            }
            manager.requestLocationUpdates(
                provider,
                0L,
                0f,
                listener,
                Looper.getMainLooper(),
            )
        }
    }
}
