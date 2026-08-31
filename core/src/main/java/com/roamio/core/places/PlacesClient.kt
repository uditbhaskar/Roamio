package com.roamio.core.places

import com.roamio.core.constants.CoreConstants
import com.roamio.core.network.OverpassElementDto
import com.roamio.core.network.OverpassGateway
import com.roamio.core.network.OverpassResponseDto
import com.roamio.core.util.GeoUtils
import com.roamio.core.wiki.WikiTitles
import io.ktor.client.call.body
import java.util.Locale

/**
 * Loads named outdoor places and nearby food from Overpass.
 *
 * @param overpassGateway Failover Overpass client.
 * @author udit
 */
class PlacesClient(
    private val overpassGateway: OverpassGateway,
) {

    /**
     * Nearby named places for [activity] around a coordinate.
     *
     * @param activity Hiking, kayaking, or biking.
     * @param latitude Search latitude.
     * @param longitude Search longitude.
     * @return Deduplicated places sorted by distance.
     * @author udit
     */
    suspend fun nearbyActivities(
        activity: ActivityKind,
        latitude: Double,
        longitude: Double,
    ): List<ExplorePlace> {
        if (activity == ActivityKind.PLACE ||
            activity == ActivityKind.POPULAR ||
            activity == ActivityKind.CAFE
        ) {
            return emptyList()
        }
        val (fast, full) = when (activity) {
            ActivityKind.HIKING ->
                CoreConstants.Overpass.QUERY_HIKING to CoreConstants.Overpass.QUERY_HIKING_FULL
            ActivityKind.KAYAKING ->
                CoreConstants.Overpass.QUERY_KAYAKING to CoreConstants.Overpass.QUERY_KAYAKING_FULL
            ActivityKind.BIKING ->
                CoreConstants.Overpass.QUERY_BIKING to CoreConstants.Overpass.QUERY_BIKING_FULL
            ActivityKind.PLACE,
            ActivityKind.POPULAR,
            ActivityKind.CAFE,
            -> return emptyList()
        }
        val quick = queryOrEmpty(fast, activity, latitude, longitude)
        if (quick.isNotEmpty()) return rankPlaces(quick, activity)
        return rankPlaces(queryOrEmpty(full, activity, latitude, longitude), activity)
    }

    /**
     * Nearby restaurants and cafés for the detail card.
     *
     * @param latitude Place latitude.
     * @param longitude Place longitude.
     * @return Up to a few named venues.
     * @author udit
     */
    suspend fun nearbyFood(latitude: Double, longitude: Double): List<NearbyBite> {
        val query = String.format(
            Locale.US,
            CoreConstants.Overpass.QUERY_RESTAURANTS,
            CoreConstants.Overpass.FOOD_RADIUS_METERS,
            latitude,
            longitude,
            CoreConstants.Overpass.FOOD_RESULT_LIMIT,
        )
        return try {
            val response: OverpassResponseDto = overpassGateway.fetch(query).body()
            response.elements.mapNotNull { element ->
                val lat = element.latitude() ?: return@mapNotNull null
                val lon = element.longitude() ?: return@mapNotNull null
                val name = element.tag(CoreConstants.Overpass.TAG_NAME)
                if (name.isBlank() || name.contains(CoreConstants.Wiki.DOMAIN_DOT)) return@mapNotNull null
                val distance = GeoUtils.haversineKm(latitude, longitude, lat, lon)
                NearbyBite(
                    name = name,
                    address = element.addressLine(),
                    cuisine = element.tag(CoreConstants.Overpass.TAG_CUISINE),
                    walkMinutes = GeoUtils.walkMinutes(distance),
                    latitude = lat,
                    longitude = lon,
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun queryOrEmpty(
        template: String,
        activity: ActivityKind,
        latitude: Double,
        longitude: Double,
    ): List<ExplorePlace> {
        val query = String.format(
            Locale.US,
            template,
            CoreConstants.Overpass.ACTIVITY_RADIUS_METERS,
            latitude,
            longitude,
            CoreConstants.Api.OVERPASS_RESULT_LIMIT,
        )
        return try {
            parsePlaces(query, activity, latitude, longitude)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun parsePlaces(
        query: String,
        activity: ActivityKind,
        fromLat: Double,
        fromLon: Double,
    ): List<ExplorePlace> {
        val response: OverpassResponseDto = overpassGateway.fetch(query).body()
        return response.elements.mapNotNull { element ->
            val lat = element.latitude() ?: return@mapNotNull null
            val lon = element.longitude() ?: return@mapNotNull null
            val name = element.tag(CoreConstants.Overpass.TAG_NAME)
            if (name.isBlank() || name.contains(CoreConstants.Wiki.DOMAIN_DOT)) return@mapNotNull null
            if (!WikiTitles.isPlaceTitle(name) || WikiTitles.isSkipName(name)) return@mapNotNull null
            ExplorePlace(
                osmId = element.id,
                osmType = element.type,
                name = name,
                latitude = lat,
                longitude = lon,
                activity = activity,
                elevationMeters = element.tag(CoreConstants.Overpass.TAG_ELE).toIntOrNull(),
                distanceKm = GeoUtils.haversineKm(fromLat, fromLon, lat, lon),
                photoUrl = element.tag(CoreConstants.Overpass.TAG_IMAGE).ifBlank { null },
                isOpenHours = element.tag(CoreConstants.Overpass.TAG_OPENING_HOURS).isNotBlank(),
                wikimediaFile = element.tag(CoreConstants.Overpass.TAG_WIKIMEDIA).ifBlank { null },
            )
        }.distinctBy { it.osmId }
    }

    private fun rankPlaces(
        places: List<ExplorePlace>,
        activity: ActivityKind,
    ): List<ExplorePlace> {
        return places.sortedWith(
            compareByDescending<ExplorePlace> { place ->
                WikiTitles.activityScore(place.name, "", activity)
            }.thenByDescending { place ->
                !place.photoUrl.isNullOrBlank() || !place.wikimediaFile.isNullOrBlank()
            }.thenBy { place ->
                place.distanceKm ?: Double.MAX_VALUE
            },
        )
    }
}

private fun OverpassElementDto.latitude(): Double? = lat ?: center?.lat

private fun OverpassElementDto.longitude(): Double? = lon ?: center?.lon

private fun OverpassElementDto.tag(key: String): String = tags[key].orEmpty()

private fun OverpassElementDto.addressLine(): String {
    val number = tag(CoreConstants.Overpass.TAG_ADDR_HOUSENUMBER)
    val street = tag(CoreConstants.Overpass.TAG_ADDR_STREET)
    val city = tag(CoreConstants.Overpass.TAG_ADDR_CITY)
    val streetLine = listOf(number, street).filter { it.isNotBlank() }.joinToString(" ")
    return listOf(streetLine, city).filter { it.isNotBlank() }.joinToString(", ")
}
