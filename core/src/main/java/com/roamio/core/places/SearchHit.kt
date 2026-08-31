package com.roamio.core.places

/**
 * Nominatim forward-search result used to recenter Home.
 *
 * @param name City or place label.
 * @param latitude Latitude in decimal degrees.
 * @param longitude Longitude in decimal degrees.
 * @param countryCode ISO country code when known.
 * @author udit
 */
data class SearchHit(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val countryCode: String,
)
