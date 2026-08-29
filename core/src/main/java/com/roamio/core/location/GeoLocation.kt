package com.roamio.core.location

/**
 * Geographic coordinates plus a human-readable place name.
 *
 * @param latitude Latitude in decimal degrees.
 * @param longitude Longitude in decimal degrees.
 * @param isFallback True when a configured city was used instead of the device.
 * @param placeName City and country shown in the UI.
 * @param countryCode ISO 3166-1 alpha-2 country code when known.
 * @author udit
 */
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val isFallback: Boolean = false,
    val placeName: String = "",
    val countryCode: String = "",
)
