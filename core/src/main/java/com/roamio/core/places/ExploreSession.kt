package com.roamio.core.places

import com.roamio.core.location.GeoLocation

/**
 * In-memory search point shared by Home and Popular.
 *
 * @author udit
 */
class ExploreSession {
    @Volatile
    var overrideLocation: GeoLocation? = null

    @Volatile
    var activity: ActivityKind = ActivityKind.PLACE

    @Volatile
    private var pendingPhotoUrl: String? = null

    /**
     * `Recenters` later Home loads on [location].
     *
     * @param location Coordinates and country chosen by search or Popular.
     * @param photoUrl Wikipedia image already known for this city, if any.
     * @author udit
     */
    fun setOverride(location: GeoLocation, photoUrl: String? = null) {
        overrideLocation = location
        pendingPhotoUrl = photoUrl
    }

    /**
     * Returns and clears the photo passed with the last [setOverride].
     *
     * @return Card image URL, or null when search did not supply one.
     * @author udit
     */
    fun takePendingPhotoUrl(): String? {
        val url = pendingPhotoUrl
        pendingPhotoUrl = null
        return url
    }
}
