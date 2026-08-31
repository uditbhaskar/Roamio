package com.roamio.core.places

/**
 * A named place the traveler can open from Home, Popular, or Saved.
 *
 * @param osmId OpenStreetMap element id.
 * @param osmType OpenStreetMap element type (node, way, or relation).
 * @param name Display title.
 * @param latitude Latitude in decimal degrees.
 * @param longitude Longitude in decimal degrees.
 * @param activity Activity chip that produced this place.
 * @param elevationMeters Elevation when OSM or Open-Meteo provides it.
 * @param distanceKm Distance from the active search point.
 * @param blurb Short Wikipedia extract.
 * @param photoUrl Remote image, or null to show the sage fallback.
 * @param photoAttribution Photographer or source credit.
 * @param countryCode ISO 3166-1 alpha-2 country code.
 * @param countryName Localized country label.
 * @param city Search city used to resolve activity photos and copy.
 * @param isOpenHours True when OSM published opening hours.
 * @param wikimediaFile OSM wikimedia_commons file name.
 * @author udit
 */
data class ExplorePlace(
    val osmId: Long,
    val osmType: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val activity: ActivityKind,
    val elevationMeters: Int? = null,
    val distanceKm: Double? = null,
    val blurb: String = "",
    val photoUrl: String? = null,
    val photoAttribution: String? = null,
    val countryCode: String = "",
    val countryName: String = "",
    val city: String = "",
    val isOpenHours: Boolean = false,
    val wikimediaFile: String? = null,
)
