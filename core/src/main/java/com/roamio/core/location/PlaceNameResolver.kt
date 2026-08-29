package com.roamio.core.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.roamio.core.constants.CoreConstants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Turns coordinates into a city name using the device geocoder, then Nominatim.
 *
 * @param context Application context for [Geocoder].
 * @param httpClient Shared Ktor client for the Nominatim fallback.
 * @author udit
 */
class PlaceNameResolver(
    private val context: Context,
    private val httpClient: HttpClient,
) {

    /**
     * Resolves a display name and country code for the given coordinates.
     *
     * @param latitude Latitude in decimal degrees.
     * @param longitude Longitude in decimal degrees.
     * @return Place label and ISO country code; empty strings when unknown.
     * @author udit
     */
    suspend fun resolve(latitude: Double, longitude: Double): ResolvedPlace {
        fromGeocoder(latitude, longitude)?.let { return it }
        fromNominatim(latitude, longitude)?.let { return it }
        return ResolvedPlace(
            placeName = CoreConstants.Location.CURRENT_LOCATION_LABEL,
            countryCode = "",
        )
    }

    private suspend fun fromGeocoder(latitude: Double, longitude: Double): ResolvedPlace? {
        return withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val addresses = Geocoder(context, Locale.getDefault())
                    .getFromLocation(latitude, longitude, 1)
                val address = addresses?.firstOrNull() ?: return@withContext null
                formatAddress(address)
            } catch (_: Exception) {
                null
            }
        }
    }

    private suspend fun fromNominatim(latitude: Double, longitude: Double): ResolvedPlace? {
        return try {
            val dto: NominatimReverseDto = httpClient.get(
                CoreConstants.Api.NOMINATIM_BASE_URL + CoreConstants.Api.NOMINATIM_REVERSE_PATH,
            ) {
                parameter(CoreConstants.Api.PARAM_LAT, latitude)
                parameter(CoreConstants.Api.PARAM_LON, longitude)
                parameter(CoreConstants.Api.PARAM_FORMAT, CoreConstants.Api.WIKI_FORMAT_JSON)
            }.body()
            val address = dto.address ?: return null
            val city = address.city
                ?: address.town
                ?: address.village
                ?: address.suburb
                ?: address.state
            val country = address.country
            val name = when {
                !city.isNullOrBlank() && !country.isNullOrBlank() -> {
                    String.format(
                        Locale.US,
                        CoreConstants.Location.PLACE_NAME_FORMAT,
                        city,
                        country,
                    )
                }
                !city.isNullOrBlank() -> city
                !country.isNullOrBlank() -> country
                else -> null
            } ?: return null
            ResolvedPlace(
                placeName = name,
                countryCode = address.countryCode.orEmpty().uppercase(Locale.US),
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun formatAddress(address: Address): ResolvedPlace? {
        val city = address.locality
            ?: address.subLocality
            ?: address.subAdminArea
            ?: address.adminArea
        val country = address.countryName
        val name = when {
            !city.isNullOrBlank() && !country.isNullOrBlank() -> {
                String.format(
                    Locale.US,
                    CoreConstants.Location.PLACE_NAME_FORMAT,
                    city,
                    country,
                )
            }
            !city.isNullOrBlank() -> city
            !country.isNullOrBlank() -> country
            else -> null
        } ?: return null
        return ResolvedPlace(
            placeName = name,
            countryCode = address.countryCode.orEmpty().uppercase(Locale.US),
        )
    }
}

/**
 * Human-readable place resolved from coordinates.
 *
 * @param placeName City and country label.
 * @param countryCode ISO 3166-1 alpha-2 country code.
 * @author udit
 */
data class ResolvedPlace(
    val placeName: String,
    val countryCode: String,
)
