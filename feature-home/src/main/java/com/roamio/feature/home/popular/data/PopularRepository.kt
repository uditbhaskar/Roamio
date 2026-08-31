package com.roamio.feature.home.popular.data

import com.roamio.core.location.GeoLocation
import com.roamio.core.location.LocationProvider
import com.roamio.core.photo.PhotoUrls
import com.roamio.core.places.ActivityKind
import com.roamio.core.places.ExploreSession
import com.roamio.core.places.SeedCities
import com.roamio.core.places.SeedCity
import com.roamio.core.result.AppResult
import com.roamio.core.result.runAppCatching
import com.roamio.core.util.GeoUtils
import com.roamio.core.weather.WeatherClient
import com.roamio.feature.home.popular.viewModel.PopularFilter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Loads bundled Popular catalog cities with optional live weather.
 *
 * @param locationProvider User point used for the Nearby filter.
 * @param weatherClient `Open-Meteo` per city.
 * @param exploreSession Recenter target when a city is opened.
 * @author udit
 */
class PopularRepository(
    private val locationProvider: LocationProvider,
    private val weatherClient: WeatherClient,
    private val exploreSession: ExploreSession,
) {

    /**
     * Fetches weather for the bundled catalog cities.
     *
     * @return City cards or a user-facing error.
     * @author udit
     */
    suspend fun load(): AppResult<PopularSnapshot> {
        return runAppCatching {
            val here = runCatching { locationProvider.getLocation() }.getOrElse {
                GeoLocation(
                    latitude = SeedCities.FALLBACK.latitude,
                    longitude = SeedCities.FALLBACK.longitude,
                    isFallback = true,
                    placeName = SeedCities.FALLBACK.name,
                    countryCode = SeedCities.FALLBACK.countryCode,
                )
            }
            val cards = coroutineScope {
                SeedCities.ALL.map { city ->
                    async {
                        val weather = runCatching {
                            weatherClient.current(city.latitude, city.longitude)
                        }.getOrNull()
                        PopularCityCard(
                            city = city,
                            temperatureC = weather?.temperatureC,
                            weatherCode = weather?.weatherCode,
                            photoUrl = PhotoUrls.display(
                                SeedCities.catalogPhotoUrl(city, ActivityKind.POPULAR),
                            ),
                            distanceKm = GeoUtils.haversineKm(
                                here.latitude,
                                here.longitude,
                                city.latitude,
                                city.longitude,
                            ),
                        )
                    }
                }.awaitAll()
            }
            PopularSnapshot(origin = here, cities = cards)
        }
    }

    /**
     * `Recenters` Home on a bundled catalog [city].
     *
     * @param city Selected catalog city.
     * @param photoUrl Catalog hero image already shown on the card.
     * @author udit
     */
    fun openCity(city: SeedCity, photoUrl: String?) {
        val image = photoUrl?.takeIf { it.isNotBlank() }
            ?: PhotoUrls.display(SeedCities.catalogPhotoUrl(city, ActivityKind.PLACE))
        exploreSession.setOverride(
            location = GeoLocation(
                latitude = city.latitude,
                longitude = city.longitude,
                isFallback = false,
                placeName = city.name,
                countryCode = city.countryCode,
            ),
            photoUrl = image,
        )
        exploreSession.activity = ActivityKind.PLACE
    }
}

/**
 * Popular payload.
 *
 * @param origin User or fallback location.
 * @param cities Catalog cards.
 * @author udit
 */
data class PopularSnapshot(
    val origin: GeoLocation,
    val cities: List<PopularCityCard>,
)

/**
 * One Popular city card.
 *
 * @param city Seed city.
 * @param temperatureC Current temperature.
 * @param weatherCode `Open-Meteo` code.
 * @param photoUrl Catalog Commons image for the card.
 * @param distanceKm Distance from the user.
 * @author udit
 */
data class PopularCityCard(
    val city: SeedCity,
    val temperatureC: Double?,
    val weatherCode: Int?,
    val photoUrl: String,
    val distanceKm: Double,
)

/**
 * Applies a Popular filter to loaded cards.
 *
 * @param filter Chip selection.
 * @return Visible cards.
 * @author udit
 */
fun List<PopularCityCard>.filtered(filter: PopularFilter): List<PopularCityCard> {
    return when (filter) {
        PopularFilter.NEARBY -> sortedBy { it.distanceKm }
        PopularFilter.CLEAR_SKIES -> filter { code ->
            val weather = code.weatherCode
            weather != null && weather <= 2
        }.ifEmpty { this }
        PopularFilter.HIKING,
        PopularFilter.KAYAKING,
        PopularFilter.BIKING,
        -> this
    }
}
