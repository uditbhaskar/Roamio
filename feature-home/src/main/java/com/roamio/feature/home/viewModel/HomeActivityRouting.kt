package com.roamio.feature.home.viewModel

import com.roamio.core.places.ExplorePlace
import com.roamio.core.places.NearbyBite
import kotlin.math.abs

/**
 * Converts a cafe [ExplorePlace] into the detail card model.
 *
 * @receiver Café row resolved on Home.
 * @return Nearby café card payload.
 * @author udit
 */
internal fun ExplorePlace.toNearbyBite(): NearbyBite {
    val parts = blurb.split(" · ").map { it.trim() }.filter { it.isNotBlank() }
    return NearbyBite(
        name = name,
        address = parts.firstOrNull().orEmpty(),
        cuisine = parts.drop(1).firstOrNull()?.ifBlank { "cafe" } ?: "cafe",
        walkMinutes = distanceKm?.let { km -> (km * 12).toInt().coerceAtLeast(1) },
        latitude = latitude,
        longitude = longitude,
        photoUrl = photoUrl,
    )
}

/**
 * Keeps the visible hero photo when a refresh returns the same place.
 *
 * @param current Place currently on screen.
 * @param incoming Freshly loaded place.
 * @return Merged place for the hero circle.
 * @author udit
 */
internal fun mergeFeaturedPlace(current: ExplorePlace?, incoming: ExplorePlace): ExplorePlace {
    if (current == null) return incoming
    if (!isSameFeaturedPlace(current, incoming)) return incoming
    return incoming.copy(
        photoUrl = incoming.photoUrl?.takeIf { it.isNotBlank() } ?: current.photoUrl,
        blurb = incoming.blurb.ifBlank { current.blurb },
    )
}

internal fun isSameFeaturedPlace(current: ExplorePlace, incoming: ExplorePlace): Boolean {
    if (
        current.city.isNotBlank() &&
        incoming.city.isNotBlank() &&
        !current.city.equals(incoming.city, ignoreCase = true)
    ) {
        return false
    }
    if (current.osmId != 0L && current.osmId == incoming.osmId) return true
    val sameCity = current.city.isNotBlank() &&
        current.city.equals(incoming.city, ignoreCase = true)
    if (!sameCity) return false
    return current.name.equals(incoming.name, ignoreCase = true) &&
        abs(current.latitude - incoming.latitude) < 0.02 &&
        abs(current.longitude - incoming.longitude) < 0.02
}
