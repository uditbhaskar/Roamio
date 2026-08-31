package com.roamio.feature.home.viewModel

import com.roamio.core.places.ExplorePlace
import com.roamio.core.places.NearbyBite

/**
 * Converts a cafe [ExplorePlace] into the detail card model.
 *
 * @receiver Cafe row resolved on Home.
 * @return Nearby cafe card payload.
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
    val samePlace = current.osmId == incoming.osmId ||
        current.name.equals(incoming.name, ignoreCase = true)
    if (!samePlace) return incoming
    return incoming.copy(
        photoUrl = incoming.photoUrl?.takeIf { it.isNotBlank() } ?: current.photoUrl,
        blurb = incoming.blurb.ifBlank { current.blurb },
    )
}
