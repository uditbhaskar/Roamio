package com.roamio.core.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.roamio.core.constants.CoreConstants
import com.roamio.core.places.NearbyBite
import com.roamio.core.places.SearchHit
import com.roamio.core.util.GeoUtils
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

    /**
     * Forward-searches Nominatim for a typed place query.
     *
     * @param query Free-text city or place name.
     * @return Matching coordinates, empty when none.
     * @author udit
     */
    suspend fun search(query: String): List<SearchHit> {
        if (query.isBlank()) return emptyList()
        val results: List<NominatimSearchDto> = httpClient.get(
            CoreConstants.Api.NOMINATIM_BASE_URL + CoreConstants.Api.NOMINATIM_SEARCH_PATH,
        ) {
            parameter(CoreConstants.Api.PARAM_Q, query)
            parameter(CoreConstants.Api.PARAM_FORMAT, CoreConstants.Api.WIKI_FORMAT_JSON)
            parameter(CoreConstants.Api.PARAM_ADDRESSDETAILS, CoreConstants.Api.ADDRESS_DETAILS_ENABLED)
            parameter(CoreConstants.Api.PARAM_LIMIT, CoreConstants.Api.NOMINATIM_SEARCH_LIMIT)
        }.body()
        return results.mapNotNull { dto ->
            val lat = dto.lat?.toDoubleOrNull() ?: return@mapNotNull null
            val lon = dto.lon?.toDoubleOrNull() ?: return@mapNotNull null
            val city = dto.address?.city
                ?: dto.address?.town
                ?: dto.address?.village
                ?: dto.displayName
            if (city.isNullOrBlank()) return@mapNotNull null
            val label = city.substringBefore(',').trim().ifBlank { city }
            if (label.contains(CoreConstants.Wiki.DOMAIN_DOT)) return@mapNotNull null
            SearchHit(
                name = label,
                latitude = lat,
                longitude = lon,
                countryCode = dto.address?.countryCode.orEmpty().uppercase(Locale.US),
            )
        }.distinctBy { hit ->
            hit.name.lowercase(Locale.US) to hit.countryCode
        }
    }

    /**
     * Finds cafes near a coordinate when Overpass has no food results.
     *
     * @param latitude Search latitude.
     * @param longitude Search longitude.
     * @return Named cafes with walk minutes, empty when none.
     * @author udit
     */
    suspend fun searchCafes(latitude: Double, longitude: Double): List<NearbyBite> {
        return try {
            val delta = CoreConstants.Api.NOMINATIM_FOOD_VIEWBOX_DEG
            val viewbox = listOf(
                longitude - delta,
                latitude + delta,
                longitude + delta,
                latitude - delta,
            ).joinToString(",")
            val results: List<NominatimSearchDto> = httpClient.get(
                CoreConstants.Api.NOMINATIM_BASE_URL + CoreConstants.Api.NOMINATIM_SEARCH_PATH,
            ) {
                parameter(CoreConstants.Api.PARAM_Q, CoreConstants.Api.NOMINATIM_FOOD_QUERY)
                parameter(CoreConstants.Api.PARAM_FORMAT, CoreConstants.Api.WIKI_FORMAT_JSON)
                parameter(CoreConstants.Api.PARAM_ADDRESSDETAILS, CoreConstants.Api.ADDRESS_DETAILS_ENABLED)
                parameter(CoreConstants.Api.PARAM_LIMIT, CoreConstants.Overpass.FOOD_RESULT_LIMIT.toString())
                parameter(CoreConstants.Api.PARAM_VIEWBOX, viewbox)
                parameter(CoreConstants.Api.PARAM_BOUNDED, CoreConstants.Api.NOMINATIM_BOUNDED)
            }.body()
            results.mapNotNull { dto ->
                val lat = dto.lat?.toDoubleOrNull() ?: return@mapNotNull null
                val lon = dto.lon?.toDoubleOrNull() ?: return@mapNotNull null
                val name = dto.displayName?.substringBefore(',')?.trim().orEmpty()
                if (name.isBlank()) return@mapNotNull null
                val distance = GeoUtils.haversineKm(latitude, longitude, lat, lon)
                NearbyBite(
                    name = name,
                    address = dto.displayName?.substringAfter(',', "")?.trim().orEmpty(),
                    cuisine = CoreConstants.Api.ACTIVITY_CAFE,
                    walkMinutes = GeoUtils.walkMinutes(distance),
                    latitude = lat,
                    longitude = lon,
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
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
