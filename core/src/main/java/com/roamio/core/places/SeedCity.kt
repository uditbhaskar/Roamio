package com.roamio.core.places

import com.roamio.core.constants.CoreConstants

/**
 * A real outdoor stop used when Overpass is empty for a catalog city.
 *
 * @param name Place title shown on Home and detail.
 * @param latitude Latitude in decimal degrees.
 * @param longitude Longitude in decimal degrees.
 * @param elevationMeters Elevation when known.
 * @author udit
 */
data class SeedLandmark(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Int? = null,
)

/**
 * Catalog city shown on Popular. Coordinates are real; photos are Commons files.
 *
 * @param name City name.
 * @param countryCode ISO country code.
 * @param latitude Latitude in decimal degrees.
 * @param longitude Longitude in decimal degrees.
 * @param photoUrl Lead image for the city and hiking.
 * @param kayakingPhotoUrl Water image for the kayaking chip.
 * @param bikingPhotoUrl Ride image for the biking chip.
 * @author udit
 */
data class SeedCity(
    val name: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String = "",
    val kayakingPhotoUrl: String = "",
    val bikingPhotoUrl: String = "",
)

/**
 * Small static set of well-known outdoor destinations with bundled photos and activities.
 *
 * @author udit
 */
object SeedCities {
    val FALLBACK: SeedCity = city(
        name = CoreConstants.Location.FALLBACK_CITY_NAME,
        countryCode = CoreConstants.Location.FALLBACK_COUNTRY_CODE,
        latitude = CoreConstants.Location.FALLBACK_LATITUDE,
        longitude = CoreConstants.Location.FALLBACK_LONGITUDE,
        photo = CoreConstants.Api.SEED_FILE_LONDON,
        water = CoreConstants.Api.SEED_FILE_LONDON_WATER,
    )

    val ALL: List<SeedCity> = listOf(
        city("Bergen", "NO", 60.3913, 5.3221, CoreConstants.Api.SEED_FILE_BERGEN, CoreConstants.Api.SEED_FILE_BERGEN_WATER),
        city("Innsbruck", "AT", 47.2692, 11.4041, CoreConstants.Api.SEED_FILE_INNSBRUCK, CoreConstants.Api.SEED_FILE_INNSBRUCK_WATER),
        city("Interlaken", "CH", 46.6863, 7.8632, CoreConstants.Api.SEED_FILE_INTERLAKEN, CoreConstants.Api.SEED_FILE_INTERLAKEN_WATER),
        city("Banff", "CA", 51.1784, -115.5708, CoreConstants.Api.SEED_FILE_BANFF, CoreConstants.Api.SEED_FILE_BANFF_WATER),
        city("Queenstown", "NZ", -45.0312, 168.6626, CoreConstants.Api.SEED_FILE_QUEENSTOWN, CoreConstants.Api.SEED_FILE_QUEENSTOWN_WATER),
        city("Reykjavik", "IS", 64.1466, -21.9426, CoreConstants.Api.SEED_FILE_REYKJAVIK, CoreConstants.Api.SEED_FILE_REYKJAVIK_WATER),
        city("Chamonix", "FR", 45.9237, 6.8694, CoreConstants.Api.SEED_FILE_CHAMONIX, CoreConstants.Api.SEED_FILE_CHAMONIX_WATER),
        city("Barcelona", "ES", 41.3874, 2.1686, CoreConstants.Api.SEED_FILE_BARCELONA, CoreConstants.Api.SEED_FILE_BARCELONA_WATER),
    )

    /**
     * Returns a catalog city when [name] matches a bundled Popular destination.
     *
     * @param name Headline or search city.
     * @return Seed city when [name] is in the catalog.
     * @author udit
     */
    fun matchCity(name: String): SeedCity? {
        val key = name.trim().substringBefore(',').trim()
        return ALL.firstOrNull { it.name.equals(key, ignoreCase = true) }
            ?: FALLBACK.takeIf { it.name.equals(key, ignoreCase = true) }
    }

    /**
     * Named trail, water, ride, or café near [cityName] for [activity].
     *
     * @param cityName Headline or catalog city.
     * @param activity Active Home chip.
     * @return Landmark when the city is in the catalog.
     * @author udit
     */
    fun landmark(cityName: String, activity: ActivityKind): SeedLandmark? {
        val key = cityName.trim().substringBefore(',').trim().lowercase()
        return LANDMARKS[key to activity]
    }

    /**
     * Commons image for [cityName] and [activity], or empty when unknown.
     *
     * @param cityName Headline or catalog city.
     * @param activity Active Home chip.
     * @return Direct Wikimedia file URL.
     * @author udit
     */
    fun photoFor(cityName: String, activity: ActivityKind): String {
        val city = matchCity(cityName)
        return city?.photoFor(activity) ?: genericPhoto(activity)
    }

    /**
     * Guaranteed Commons image for a catalog [city] and [activity].
     *
     * @param city Catalog city.
     * @param activity Active chip or place overview.
     * @return Direct Wikimedia file URL.
     * @author udit
     */
    fun catalogPhotoUrl(city: SeedCity, activity: ActivityKind): String {
        return city.photoFor(activity).ifBlank { genericPhoto(activity) }
    }

    /**
     * Activity stock image used when the city is not in the catalog.
     *
     * @param activity Active Home chip.
     * @return Direct Wikimedia file URL.
     * @author udit
     */
    fun genericPhoto(activity: ActivityKind): String {
        val file = when (activity) {
            ActivityKind.PLACE,
            ActivityKind.POPULAR,
            -> CoreConstants.Api.FALLBACK_CITY_FILE
            ActivityKind.CAFE -> CoreConstants.Api.FALLBACK_CAFE_FILE
            ActivityKind.HIKING -> CoreConstants.Api.FALLBACK_HIKING_FILE
            ActivityKind.KAYAKING -> CoreConstants.Api.FALLBACK_KAYAKING_FILE
            ActivityKind.BIKING -> CoreConstants.Api.FALLBACK_BIKING_FILE
        }
        return CoreConstants.Api.WIKIMEDIA_FILE_PATH + file
    }

    /**
     * Short generic extract for [activity] when Wikipedia is empty.
     *
     * @param activity Active Home chip.
     * @return Fallback blurb.
     * @author udit
     */
    fun genericBlurb(activity: ActivityKind): String {
        return when (activity) {
            ActivityKind.PLACE -> CoreConstants.Copy.CITY
            ActivityKind.POPULAR -> CoreConstants.Copy.POPULAR
            ActivityKind.CAFE -> CoreConstants.Copy.CAFE
            ActivityKind.HIKING -> CoreConstants.Copy.HIKING
            ActivityKind.KAYAKING -> CoreConstants.Copy.KAYAKING
            ActivityKind.BIKING -> CoreConstants.Copy.BIKING
        }
    }

    /**
     * Picks the Commons image for [activity] on this city.
     *
     * @param activity Active Home chip.
     * @return Direct Wikimedia file URL.
     * @author udit
     */
    fun SeedCity.photoFor(activity: ActivityKind): String {
        return when (activity) {
            ActivityKind.PLACE,
            ActivityKind.POPULAR,
            ActivityKind.HIKING,
            -> photoUrl
            ActivityKind.CAFE -> genericPhoto(ActivityKind.CAFE)
            ActivityKind.KAYAKING -> kayakingPhotoUrl.ifBlank { photoUrl }
            ActivityKind.BIKING -> bikingPhotoUrl.ifBlank { photoUrl }
        }
    }

    private val LANDMARKS: Map<Pair<String, ActivityKind>, SeedLandmark> = mapOf(
        ("bergen" to ActivityKind.HIKING) to SeedLandmark("Fløyen", 60.3945, 5.3435, 400),
        ("bergen" to ActivityKind.KAYAKING) to SeedLandmark("Byfjorden", 60.4000, 5.2900),
        ("bergen" to ActivityKind.BIKING) to SeedLandmark("Ulriken", 60.3775, 5.3872, 643),
        ("bergen" to ActivityKind.CAFE) to SeedLandmark("Bergen Kaffebrenneriet", 60.3975, 5.3242),
        ("innsbruck" to ActivityKind.HIKING) to SeedLandmark("Nordkette", 47.3120, 11.3780, 2334),
        ("innsbruck" to ActivityKind.KAYAKING) to SeedLandmark("Inn", 47.2692, 11.3950),
        ("innsbruck" to ActivityKind.BIKING) to SeedLandmark("Innradweg", 47.2692, 11.4041),
        ("innsbruck" to ActivityKind.CAFE) to SeedLandmark("Café M32", 47.2685, 11.3930),
        ("interlaken" to ActivityKind.HIKING) to SeedLandmark("Harder Kulm", 46.6983, 7.8660, 1322),
        ("interlaken" to ActivityKind.KAYAKING) to SeedLandmark("Lake Thun", 46.7270, 7.7260),
        ("interlaken" to ActivityKind.BIKING) to SeedLandmark("Hardergrat", 46.6983, 7.8660),
        ("interlaken" to ActivityKind.CAFE) to SeedLandmark("Velo Café", 46.6840, 7.8550),
        ("banff" to ActivityKind.HIKING) to SeedLandmark("Sulphur Mountain", 51.1480, -115.5760, 2451),
        ("banff" to ActivityKind.KAYAKING) to SeedLandmark("Lake Louise", 51.4254, -116.1773),
        ("banff" to ActivityKind.BIKING) to SeedLandmark("Banff Legacy Trail", 51.1784, -115.5708),
        ("banff" to ActivityKind.CAFE) to SeedLandmark("Whitebark Café", 51.1765, -115.5715),
        ("queenstown" to ActivityKind.HIKING) to SeedLandmark("Queenstown Hill", -45.0220, 168.6800, 907),
        ("queenstown" to ActivityKind.KAYAKING) to SeedLandmark("Lake Wakatipu", -45.0500, 168.6500),
        ("queenstown" to ActivityKind.BIKING) to SeedLandmark("Queenstown Trail", -45.0312, 168.6626),
        ("queenstown" to ActivityKind.CAFE) to SeedLandmark("Patagonia Coffee", -45.0318, 168.6620),
        ("reykjavik" to ActivityKind.HIKING) to SeedLandmark("Esjan", 64.2960, -21.6920, 914),
        ("reykjavik" to ActivityKind.KAYAKING) to SeedLandmark("Faxaflói", 64.1500, -22.2000),
        ("reykjavik" to ActivityKind.BIKING) to SeedLandmark("Ellidaár", 64.1100, -21.8700),
        ("reykjavik" to ActivityKind.CAFE) to SeedLandmark("Reykjavik Roasters", 64.1445, -21.9280),
        ("chamonix" to ActivityKind.HIKING) to SeedLandmark("Aiguille du Midi", 45.8790, 6.8870, 3842),
        ("chamonix" to ActivityKind.KAYAKING) to SeedLandmark("Arve River", 45.9200, 6.8700),
        ("chamonix" to ActivityKind.BIKING) to SeedLandmark("Valley Cycle Path", 45.9237, 6.8694),
        ("chamonix" to ActivityKind.CAFE) to SeedLandmark("Café du Mont-Blanc", 45.9230, 6.8710),
        ("barcelona" to ActivityKind.HIKING) to SeedLandmark("Tibidabo", 41.4225, 2.1186, 512),
        ("barcelona" to ActivityKind.KAYAKING) to SeedLandmark("Mediterranean Coast", 41.3800, 2.1950),
        ("barcelona" to ActivityKind.BIKING) to SeedLandmark("Ciutadella Park", 41.3880, 2.1860),
        ("barcelona" to ActivityKind.CAFE) to SeedLandmark("Satan's Coffee Corner", 41.3905, 2.1640),
        ("london" to ActivityKind.HIKING) to SeedLandmark("Hampstead Heath", 51.5608, -0.1610),
        ("london" to ActivityKind.KAYAKING) to SeedLandmark("River Thames", 51.5074, -0.1278),
        ("london" to ActivityKind.BIKING) to SeedLandmark("Richmond Park", 51.4490, -0.2730),
        ("london" to ActivityKind.CAFE) to SeedLandmark("Monmouth Coffee", 51.5145, -0.1265),
    )

    private fun city(
        name: String,
        countryCode: String,
        latitude: Double,
        longitude: Double,
        photo: String,
        water: String,
    ): SeedCity {
        val lead = CoreConstants.Api.WIKIMEDIA_FILE_PATH + photo
        return SeedCity(
            name = name,
            countryCode = countryCode,
            latitude = latitude,
            longitude = longitude,
            photoUrl = lead,
            kayakingPhotoUrl = CoreConstants.Api.WIKIMEDIA_FILE_PATH + water,
            bikingPhotoUrl = lead,
        )
    }
}
