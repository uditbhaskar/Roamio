package com.roamio.core.util

import com.roamio.core.constants.CoreConstants
import com.roamio.core.places.ExplorePlace
import com.roamio.core.places.SavedPlaceRecord
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Distance, walk-time, and country-flag helpers used by feature screens.
 *
 * @author udit
 */
object GeoUtils {

    /**
     * Great-circle distance in kilometers.
     *
     * @param fromLat Start latitude.
     * @param fromLon Start longitude.
     * @param toLat End latitude.
     * @param toLon End longitude.
     * @return Distance in kilometers.
     * @author udit
     */
    fun haversineKm(
        fromLat: Double,
        fromLon: Double,
        toLat: Double,
        toLon: Double,
    ): Double {
        val lat1 = Math.toRadians(fromLat)
        val lat2 = Math.toRadians(toLat)
        val dLat = Math.toRadians(toLat - fromLat)
        val dLon = Math.toRadians(toLon - fromLon)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return CoreConstants.Geo.EARTH_RADIUS_KM * c
    }

    /**
     * Formats kilometers for meta rows.
     *
     * @param kilometers Distance value.
     * @return One-decimal kilometer string.
     * @author udit
     */
    fun formatKm(kilometers: Double): String {
        return String.format(Locale.US, CoreConstants.Geo.KM_FORMAT, kilometers)
    }

    /**
     * Formats an elevation in meters.
     *
     * @param meters Elevation above sea level.
     * @return Grouped integer string.
     * @author udit
     */
    fun formatElevation(meters: Int): String {
        return String.format(Locale.US, CoreConstants.Geo.ELEVATION_FORMAT, meters)
    }

    /**
     * Walk minutes from a kilometer distance using a steady pace.
     *
     * @param kilometers Distance to walk.
     * @return Rounded minute count.
     * @author udit
     */
    fun walkMinutes(kilometers: Double): Int {
        return (kilometers * CoreConstants.Geo.WALK_MINUTES_PER_KM).toInt().coerceAtLeast(1)
    }

    /**
     * Regional-indicator flag emoji for an ISO country code.
     *
     * @param countryCode Two-letter ISO code.
     * @return Flag emoji, or empty when the code is invalid.
     * @author udit
     */
    fun countryFlag(countryCode: String): String {
        val code = countryCode.uppercase(Locale.US)
        if (code.length != CoreConstants.Geo.COUNTRY_CODE_LENGTH) return ""
        val first = Character.codePointAt(code, 0) - CoreConstants.Geo.LATIN_A +
            CoreConstants.Geo.FLAG_REGIONAL_INDICATOR_A
        val second = Character.codePointAt(code, 1) - CoreConstants.Geo.LATIN_A +
            CoreConstants.Geo.FLAG_REGIONAL_INDICATOR_A
        return String(intArrayOf(first, second), 0, 2)
    }

    /**
     * Localized country name from an ISO code.
     *
     * @param countryCode Two-letter ISO code.
     * @return Display country, or the raw code when unknown.
     * @author udit
     */
    fun countryName(countryCode: String): String {
        if (countryCode.length != CoreConstants.Geo.COUNTRY_CODE_LENGTH) return countryCode
        return Locale.Builder()
            .setRegion(countryCode.uppercase(Locale.US))
            .build()
            .displayCountry
            .ifBlank { countryCode }
    }

    /**
     * Stable OSM-style id for a named coordinate when Overpass did not supply one.
     *
     * @param name Place or city label.
     * @param latitude Latitude in decimal degrees.
     * @param longitude Longitude in decimal degrees.
     * @return Non-zero id used for save matching.
     * @author udit
     */
    fun fallbackOsmId(name: String, latitude: Double, longitude: Double): Long {
        val scaledLat = (latitude * CoreConstants.Geo.GEO_ID_SCALE).roundToLong()
        val scaledLon = (longitude * CoreConstants.Geo.GEO_ID_SCALE).roundToLong()
        val hashed = name.lowercase(Locale.US).hashCode().toLong()
        val id = scaledLat * 1_000_003L + scaledLon * 97L + hashed
        return if (id == 0L) 1L else id
    }

    /**
     * Whether [place] is the same saved row as [record].
     *
     * @param place Live place from detail.
     * @param record Persisted saved row.
     * @return True when ids match, or name and coordinates match for fallbacks.
     * @author udit
     */
    fun sameSavedPlace(place: ExplorePlace, record: SavedPlaceRecord): Boolean {
        val placeKey = saveKey(place.name, place.osmType, place.osmId, place.latitude, place.longitude)
        val recordKey = record.saveKey.ifBlank {
            saveKey(record.name, record.osmType, record.osmId, record.latitude, record.longitude)
        }
        if (placeKey == recordKey) return true
        if (
            place.osmId != CoreConstants.Overpass.FALLBACK_ELEMENT_ID &&
            record.osmId != CoreConstants.Overpass.FALLBACK_ELEMENT_ID &&
            place.osmId == record.osmId &&
            place.osmType == record.osmType
        ) {
            return true
        }
        return place.name.equals(record.name, ignoreCase = true) &&
            abs(place.latitude - record.latitude) < CoreConstants.Geo.SAVE_COORD_EPSILON &&
            abs(place.longitude - record.longitude) < CoreConstants.Geo.SAVE_COORD_EPSILON
    }

    /**
     * Stable key used to persist and match a saved place.
     *
     * @param name Place label.
     * @param osmType OpenStreetMap type.
     * @param osmId OpenStreetMap id, or a generated fallback.
     * @param latitude Latitude in decimal degrees.
     * @param longitude Longitude in decimal degrees.
     * @return Key that stays the same across detail refreshes.
     * @author udit
     */
    fun saveKey(
        name: String,
        osmType: String,
        osmId: Long,
        latitude: Double,
        longitude: Double,
    ): String {
        if (osmId != CoreConstants.Overpass.FALLBACK_ELEMENT_ID) {
            return "$osmType:$osmId"
        }
        val scaledLat = (latitude * CoreConstants.Geo.GEO_ID_SCALE).roundToLong()
        val scaledLon = (longitude * CoreConstants.Geo.GEO_ID_SCALE).roundToLong()
        return listOf(
            name.lowercase(Locale.US),
            scaledLat.toString(),
            scaledLon.toString(),
        ).joinToString(CoreConstants.Logging.CACHE_KEY_SEPARATOR)
    }
}
