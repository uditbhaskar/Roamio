package com.roamio.core.places

/**
 * Nearest cafe or restaurant shown in the detail hotel-card slot.
 *
 * @param name Venue name.
 * @param address Street or city line.
 * @param cuisine OSM cuisine tag, when present.
 * @param walkMinutes Estimated walk from the place.
 * @param latitude Venue latitude.
 * @param longitude Venue longitude.
 * @param photoUrl Resolved venue or cafe image.
 * @author udit
 */
data class NearbyBite(
    val name: String,
    val address: String,
    val cuisine: String,
    val walkMinutes: Int?,
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String? = null,
)
