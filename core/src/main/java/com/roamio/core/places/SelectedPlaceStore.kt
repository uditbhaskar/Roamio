package com.roamio.core.places

/**
 * Holds the place about to be opened on the detail route.
 *
 * @author udit
 */
class SelectedPlaceStore {
    @Volatile
    var place: ExplorePlace? = null
    @Volatile
    var includeNearbyCafe: Boolean = false
    @Volatile
    var detailFocus: ActivityKind? = null
    @Volatile
    var seededNearbyCafe: NearbyBite? = null

    /**
     * Reads and clears the café section flag for the next detail load.
     *
     * @return True when detail should fetch a nearby café card.
     * @author udit
     */
    fun takeIncludeNearbyCafe(): Boolean {
        val value = includeNearbyCafe
        includeNearbyCafe = false
        return value
    }

    /**
     * Reads and clears the detail section opened from a Home chip.
     *
     * @return Activity section to highlight, or null for a generic place view.
     * @author udit
     */
    fun takeDetailFocus(): ActivityKind? {
        val value = detailFocus
        detailFocus = null
        return value
    }

    /**
     * Reads and clears a café row prefetched on Home.
     *
     * @return Cached café bite when Home already resolved one.
     * @author udit
     */
    fun takeSeededNearbyCafe(): NearbyBite? {
        val value = seededNearbyCafe
        seededNearbyCafe = null
        return value
    }
}
