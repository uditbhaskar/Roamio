package com.roamio.core.location

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Nominatim reverse-geocode JSON payload.
 *
 * @author udit
 */
@Serializable
data class NominatimReverseDto(
    val address: NominatimAddressDto? = null,
)

/**
 * One Nominatim forward-search hit.
 *
 * @author udit
 */
@Serializable
data class NominatimSearchDto(
    val lat: String? = null,
    val lon: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val address: NominatimAddressDto? = null,
)

/**
 * Address fields returned by Nominatim.
 *
 * @author udit
 */
@Serializable
data class NominatimAddressDto(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val suburb: String? = null,
    val state: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val country: String? = null,
)
