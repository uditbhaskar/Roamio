package com.roamio.feature.home.data

import com.roamio.core.constants.CoreConstants
import com.roamio.core.location.GeoLocation
import com.roamio.core.location.LocationProvider
import com.roamio.core.location.PlaceNameResolver
import com.roamio.core.photo.PhotoUrls
import com.roamio.core.photo.PlacePhotoResolver
import com.roamio.core.places.ActivityKind
import com.roamio.core.places.ExplorePlace
import com.roamio.core.places.ExploreSession
import com.roamio.core.places.NearbyBite
import com.roamio.core.places.PlacesClient
import com.roamio.core.places.SearchHit
import com.roamio.core.places.SeedCities
import com.roamio.core.places.SeedLandmark
import com.roamio.core.preferences.AppPreferences
import com.roamio.core.result.AppResult
import com.roamio.core.util.GeoUtils
import com.roamio.core.weather.CurrentWeather
import com.roamio.core.weather.WeatherClient
import com.roamio.core.wiki.WikiTitles
import com.roamio.core.wiki.WikipediaClient
import com.roamio.core.wiki.WikipediaNearby
import com.roamio.core.wiki.WikipediaSummary
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Loads Home weather, location, and the featured activity place.
 *
 * @param locationProvider Device or fallback coordinates.
 * @param placeNameResolver Reverse geocode and city search.
 * @param weatherClient Open-Meteo current conditions.
 * @param wikipediaClient Place extract, image, and activity search.
 * @param placesClient Nearby hiking, water, and ride stops.
 * @param photoResolver Activity-aware card image.
 * @param exploreSession Shared search-point override.
 * @param appPreferences Greeting name and units.
 * @author udit
 */
class HomeRepository(
    private val locationProvider: LocationProvider,
    private val placeNameResolver: PlaceNameResolver,
    private val weatherClient: WeatherClient,
    private val wikipediaClient: WikipediaClient,
    private val placesClient: PlacesClient,
    private val photoResolver: PlacePhotoResolver,
    private val exploreSession: ExploreSession,
    private val appPreferences: AppPreferences,
) {

    /**
     * Builds a Home snapshot around the active search point.
     *
     * @param activity Selected chip; only picks the featured row from cached resolution.
     * @return Snapshot or a user-facing error.
     * @author udit
     */
    suspend fun load(activity: ActivityKind): AppResult<HomeSnapshot> {
        val location = exploreSession.overrideLocation
            ?: runCatching { locationProvider.getLocation() }.getOrElse { fallbackLocation() }
        exploreSession.activity = activity
        val snapshot = runCatching {
            coroutineScope {
                val weatherDeferred = async {
                    runCatching { weatherClient.current(location.latitude, location.longitude) }.getOrNull()
                }
                val placeDeferred = async { featuredPlace(location) }
                val nameDeferred = async {
                    runCatching { appPreferences.displayName.first() }.getOrDefault("")
                }
                val unitDeferred = async {
                    runCatching { appPreferences.useCelsius.first() }.getOrDefault(true)
                }
                val resolved = placeDeferred.await()
                HomeSnapshot(
                    location = location,
                    weather = weatherDeferred.await(),
                    cityPlace = resolved.cityPlace,
                    activityPlaces = resolved.activityPlaces,
                    cafePlace = resolved.cafePlace,
                    loadedActivities = resolved.loadedActivities,
                    displayName = nameDeferred.await(),
                    useCelsius = unitDeferred.await(),
                )
            }
        }.getOrElse {
            val fallback = runCatching { featuredPlace(location) }.getOrNull()
            HomeSnapshot(
                location = location,
                weather = null,
                cityPlace = fallback?.cityPlace,
                activityPlaces = fallback?.activityPlaces.orEmpty(),
                cafePlace = fallback?.cafePlace,
                loadedActivities = fallback?.loadedActivities.orEmpty(),
                displayName = "",
                useCelsius = true,
            )
        }
        return AppResult.Success(snapshot)
    }

    /**
     * Nominatim search for the Home overlay.
     *
     * @param query Typed city text.
     * @return Hits or a user-facing error.
     * @author udit
     */
    suspend fun search(query: String): AppResult<List<SearchHit>> {
        val hits = runCatching { placeNameResolver.search(query) }.getOrDefault(emptyList())
        if (hits.isNotEmpty()) return AppResult.Success(hits)
        val seeded = SeedCities.ALL
            .filter { city -> city.name.contains(query.trim(), ignoreCase = true) }
            .ifEmpty { SeedCities.ALL }
            .map { city ->
                SearchHit(
                    name = city.name,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    countryCode = city.countryCode,
                )
            }
        return AppResult.Success(seeded)
    }

    /**
     * Recenters later loads on [hit].
     *
     * @param hit Selected search result.
     * @param photoUrl Optional hero image for the chosen city.
     * @author udit
     */
    fun applySearch(hit: SearchHit, photoUrl: String? = null) {
        exploreSession.setOverride(
            GeoLocation(
                latitude = hit.latitude,
                longitude = hit.longitude,
                isFallback = false,
                placeName = hit.name,
                countryCode = hit.countryCode,
            ),
            photoUrl = photoUrl,
        )
    }

    private suspend fun featuredPlace(
        location: GeoLocation,
    ): FeaturedResolution {
        val city = location.placeName.substringBefore(',').trim().ifBlank { location.placeName }
        val wikiHits = runCatching {
            wikipediaClient.nearbySummaries(location.latitude, location.longitude)
        }.getOrDefault(emptyList())
        val resolvedChips = resolveChips(city, location, wikiHits)
        val cityPlace = cityPlace(city, location, wikiHits)
        val activityPlaces = resolvedChips.filterKeys { it != ActivityKind.CAFE }
        return FeaturedResolution(
            cityPlace = cityPlace,
            activityPlaces = activityPlaces,
            cafePlace = resolvedChips[ActivityKind.CAFE],
            loadedActivities = resolvedChips.keys,
        )
    }

    private suspend fun resolveChips(
        city: String,
        location: GeoLocation,
        wikiHits: List<WikipediaNearby>,
    ): Map<ActivityKind, ExplorePlace> {
        return coroutineScope {
            listOf(
                ActivityKind.POPULAR,
                ActivityKind.CAFE,
                ActivityKind.HIKING,
                ActivityKind.KAYAKING,
                ActivityKind.BIKING,
            ).map { kind ->
                async { kind to resolveChip(kind, city, location, wikiHits) }
            }.mapNotNull { deferred ->
                val (kind, place) = deferred.await()
                place?.let { kind to it }
            }.toMap()
        }
    }

    private suspend fun cityPlace(
        city: String,
        location: GeoLocation,
        wikiHits: List<WikipediaNearby>,
    ): ExplorePlace {
        SeedCities.matchCity(city)?.let { catalog ->
            return catalogExplorePlace(
                seed = ExplorePlace(
                    osmId = GeoUtils.fallbackOsmId(catalog.name, catalog.latitude, catalog.longitude),
                    osmType = CoreConstants.Overpass.TYPE_NODE,
                    name = catalog.name,
                    latitude = catalog.latitude,
                    longitude = catalog.longitude,
                    activity = ActivityKind.PLACE,
                    blurb = SeedCities.genericBlurb(ActivityKind.PLACE),
                    photoUrl = PhotoUrls.display(SeedCities.catalogPhotoUrl(catalog, ActivityKind.PLACE)),
                    countryCode = catalog.countryCode,
                    city = catalog.name,
                ),
                catalogCity = catalog.name,
                location = location,
            )
        }
        val wiki = matchWiki(city, wikiHits)
            ?: runCatching { wikipediaClient.summary(city) }.getOrNull()
                ?.takeIf { WikiTitles.summaryFits(it, city, city) }
            ?: runCatching { wikipediaClient.searchSummary(city) }.getOrNull()
                ?.takeIf { WikiTitles.summaryFits(it, city, city) }
        return enrich(
            seed = ExplorePlace(
                osmId = GeoUtils.fallbackOsmId(city, location.latitude, location.longitude),
                osmType = CoreConstants.Overpass.TYPE_NODE,
                name = city,
                latitude = location.latitude,
                longitude = location.longitude,
                activity = ActivityKind.PLACE,
                countryCode = location.countryCode,
                city = city,
                photoUrl = wiki?.imageUrl,
            ),
            city = city,
            location = location,
            activity = ActivityKind.PLACE,
            wikiHint = wiki,
        )
    }

    private suspend fun resolveChip(
        activity: ActivityKind,
        city: String,
        location: GeoLocation,
        wikiHits: List<WikipediaNearby>,
    ): ExplorePlace? {
        return when (activity) {
            ActivityKind.HIKING,
            ActivityKind.KAYAKING,
            ActivityKind.BIKING,
            -> resolveActivityLive(activity, city, location, wikiHits)
            ActivityKind.POPULAR -> resolvePopularLive(city, location)
            ActivityKind.CAFE -> resolveCafeLive(city, location)
            ActivityKind.PLACE -> null
        }
    }

    private fun resolvePopularLive(
        city: String,
        location: GeoLocation,
    ): ExplorePlace? {
        SeedCities.matchCity(city)?.let { catalog ->
            return catalogExplorePlace(
                seed = ExplorePlace(
                    osmId = GeoUtils.fallbackOsmId(catalog.name, catalog.latitude, catalog.longitude),
                    osmType = CoreConstants.Overpass.TYPE_NODE,
                    name = catalog.name,
                    latitude = catalog.latitude,
                    longitude = catalog.longitude,
                    activity = ActivityKind.POPULAR,
                    blurb = SeedCities.genericBlurb(ActivityKind.POPULAR),
                    countryCode = catalog.countryCode,
                    city = catalog.name,
                ),
                catalogCity = catalog.name,
                location = location,
            )
        }
        val nearest = SeedCities.ALL
            .filter { seed -> !seed.name.equals(city, ignoreCase = true) }
            .minByOrNull { seed ->
                GeoUtils.haversineKm(
                    location.latitude,
                    location.longitude,
                    seed.latitude,
                    seed.longitude,
                )
            }
            ?: return null
        val distanceKm = GeoUtils.haversineKm(
            location.latitude,
            location.longitude,
            nearest.latitude,
            nearest.longitude,
        )
        if (distanceKm > CoreConstants.Geo.POPULAR_MAX_KM) return null
        return catalogExplorePlace(
            seed = ExplorePlace(
                osmId = GeoUtils.fallbackOsmId(nearest.name, nearest.latitude, nearest.longitude),
                osmType = CoreConstants.Overpass.TYPE_NODE,
                name = nearest.name,
                latitude = nearest.latitude,
                longitude = nearest.longitude,
                activity = ActivityKind.POPULAR,
                distanceKm = distanceKm,
                blurb = SeedCities.genericBlurb(ActivityKind.POPULAR),
                countryCode = nearest.countryCode,
                city = nearest.name,
            ),
            catalogCity = nearest.name,
            location = location,
        )
    }

    private suspend fun resolveCafeLive(
        city: String,
        location: GeoLocation,
    ): ExplorePlace? {
        SeedCities.landmark(city, ActivityKind.CAFE)?.let { landmark ->
            return catalogExplorePlace(
                seed = landmark.asPlace(city, location, ActivityKind.CAFE).copy(
                    blurb = SeedCities.genericBlurb(ActivityKind.CAFE),
                    isOpenHours = true,
                ),
                catalogCity = city,
                location = location,
            )
        }
        val bite = withTimeoutOrNull(CoreConstants.Api.PLACE_FOOD_TIMEOUT_MILLIS) {
            placesClient.nearbyFood(location.latitude, location.longitude)
        }.orEmpty().firstOrNull()
            ?: runCatching {
                placeNameResolver.searchCafes(location.latitude, location.longitude)
            }.getOrDefault(emptyList()).firstOrNull()
            ?: return null
        return bite.toExplorePlace(city, location)
    }

    private fun NearbyBite.toExplorePlace(
        city: String,
        location: GeoLocation,
    ): ExplorePlace {
        val cafePhoto = CoreConstants.Api.WIKIMEDIA_FILE_PATH + CoreConstants.Api.FALLBACK_CAFE_FILE
        val distanceKm = GeoUtils.haversineKm(
            location.latitude,
            location.longitude,
            latitude,
            longitude,
        )
        val blurb = listOf(cuisine, address).filter { it.isNotBlank() }.joinToString(" · ")
            .ifBlank { SeedCities.genericBlurb(ActivityKind.CAFE) }
        return ExplorePlace(
            osmId = GeoUtils.fallbackOsmId(name, latitude, longitude),
            osmType = CoreConstants.Overpass.TYPE_NODE,
            name = name,
            latitude = latitude,
            longitude = longitude,
            activity = ActivityKind.CAFE,
            distanceKm = distanceKm,
            blurb = blurb,
            photoUrl = PhotoUrls.display(cafePhoto),
            countryCode = location.countryCode,
            countryName = GeoUtils.countryName(location.countryCode).ifBlank {
                location.placeName.substringAfter(',', missingDelimiterValue = "").trim()
            },
            city = city,
            isOpenHours = true,
        )
    }

    private suspend fun resolveActivityLive(
        activity: ActivityKind,
        city: String,
        location: GeoLocation,
        wikiHits: List<WikipediaNearby>,
    ): ExplorePlace? {
        SeedCities.landmark(city, activity)?.let { landmark ->
            return catalogExplorePlace(
                seed = landmark.asPlace(city, location, activity),
                catalogCity = city,
                location = location,
            )
        }
        val osm = withTimeoutOrNull(CoreConstants.Api.HOME_NEARBY_TIMEOUT_MILLIS) {
            runCatching {
                placesClient.nearbyActivities(activity, location.latitude, location.longitude)
            }.getOrDefault(emptyList())
        }.orEmpty().firstOrNull()
        if (osm != null) {
            return enrich(
                seed = osm,
                city = city,
                location = location,
                activity = activity,
                wikiHint = matchWiki(osm.name, wikiHits),
            )
        }
        pickWikiPlace(wikiHits, city, activity, location)?.let { hit ->
            return enrich(
                seed = hit.toPlace(city, location, activity),
                city = city,
                location = location,
                activity = activity,
                wikiHint = hit.toSummary(),
            )
        }
        return null
    }

    private fun catalogExplorePlace(
        seed: ExplorePlace,
        catalogCity: String,
        location: GeoLocation,
    ): ExplorePlace {
        val activity = seed.activity
        val photo = SeedCities.photoFor(catalogCity, activity)
            .ifBlank { SeedCities.genericPhoto(activity) }
        val countryName = GeoUtils.countryName(location.countryCode).ifBlank {
            location.placeName.substringAfter(',', missingDelimiterValue = "").trim()
        }
        return seed.copy(
            blurb = seed.blurb.ifBlank { SeedCities.genericBlurb(activity) },
            photoUrl = PhotoUrls.display(photo),
            elevationMeters = seed.elevationMeters,
            distanceKm = seed.distanceKm ?: GeoUtils.haversineKm(
                location.latitude,
                location.longitude,
                seed.latitude,
                seed.longitude,
            ).takeIf { it >= CoreConstants.Geo.SAVE_COORD_EPSILON * 100 },
            countryCode = location.countryCode,
            countryName = countryName,
            city = catalogCity,
        )
    }

    private suspend fun enrich(
        seed: ExplorePlace,
        city: String,
        location: GeoLocation,
        activity: ActivityKind,
        wikiHint: WikipediaSummary? = null,
    ): ExplorePlace {
        val wiki = (wikiHint
            ?: runCatching { wikipediaClient.summary(seed.name) }.getOrNull()
            ?: runCatching { wikipediaClient.searchSummary("${seed.name} $city") }.getOrNull())
            ?.takeIf { WikiTitles.summaryFits(it, seed.name, city) }
        val voyage = if (wiki?.extract.isNullOrBlank()) {
            runCatching { wikipediaClient.wikivoyageSummary(city) }.getOrNull()
        } else {
            null
        }
        val extract = wiki?.extract.orEmpty()
            .ifBlank { voyage?.extract.orEmpty() }
            .ifBlank { SeedCities.genericBlurb(activity) }
        val placePhoto = when (activity) {
            ActivityKind.PLACE,
            ActivityKind.POPULAR,
            -> wiki?.imageUrl
            ActivityKind.CAFE -> seed.photoUrl
            else -> wiki?.takeIf { WikiTitles.summaryFitsPlace(it, seed.name) }?.imageUrl
        }
        val photo = seed.photoUrl?.takeIf { it.isNotBlank() }
            ?: placePhoto?.takeIf { it.isNotBlank() }
            ?: runCatching {
                photoResolver.resolve(
                    placeName = seed.name,
                    city = city,
                    activity = activity.name.lowercase(),
                    osmImage = seed.photoUrl,
                    wikimediaFile = seed.wikimediaFile,
                    latitude = seed.latitude,
                    longitude = seed.longitude,
                ).url
            }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: SeedCities.photoFor(city, activity).ifBlank { SeedCities.genericPhoto(activity) }
        val elevation = seed.elevationMeters ?: runCatching {
            weatherClient.elevationMeters(seed.latitude, seed.longitude)
        }.getOrNull()
        val distance = seed.distanceKm ?: GeoUtils.haversineKm(
            location.latitude,
            location.longitude,
            seed.latitude,
            seed.longitude,
        ).takeIf { it >= CoreConstants.Geo.SAVE_COORD_EPSILON * 100 }
        val wikiTitle = wiki?.title?.takeIf { WikiTitles.isPlaceTitle(it) }
        val displayName = when {
            wikiTitle == null -> seed.name
            seed.name.equals(city, ignoreCase = true) &&
                wikiTitle.contains(city, ignoreCase = true) -> wikiTitle
            wikiTitle.contains(seed.name, ignoreCase = true) -> wikiTitle
            else -> seed.name
        }
        return seed.copy(
            name = displayName,
            blurb = extract,
            photoUrl = PhotoUrls.display(photo),
            elevationMeters = elevation,
            distanceKm = distance,
            countryCode = location.countryCode,
            countryName = GeoUtils.countryName(location.countryCode).ifBlank {
                location.placeName.substringAfter(',', missingDelimiterValue = "").trim()
            },
            city = city,
        )
    }

    private fun SeedLandmark.asPlace(
        city: String,
        location: GeoLocation,
        activity: ActivityKind,
    ): ExplorePlace {
        return ExplorePlace(
            osmId = GeoUtils.fallbackOsmId(name, latitude, longitude),
            osmType = CoreConstants.Overpass.TYPE_NODE,
            name = name,
            latitude = latitude,
            longitude = longitude,
            activity = activity,
            elevationMeters = elevationMeters,
            distanceKm = GeoUtils.haversineKm(
                location.latitude,
                location.longitude,
                latitude,
                longitude,
            ),
            countryCode = location.countryCode,
            city = city,
        )
    }

    private fun pickWikiPlace(
        hits: List<WikipediaNearby>,
        city: String,
        activity: ActivityKind,
        location: GeoLocation,
    ): WikipediaNearby? {
        return hits
            .filter { hit ->
                WikiTitles.isPlaceTitle(hit.title) &&
                    !hit.title.equals(city, ignoreCase = true) &&
                    WikiTitles.activityScore(hit.title, hit.extract, activity) > 0
            }
            .sortedWith(
                compareByDescending<WikipediaNearby> { hit ->
                    WikiTitles.activityScore(hit.title, hit.extract, activity)
                }.thenByDescending { hit ->
                    !hit.imageUrl.isNullOrBlank()
                }.thenBy { hit ->
                    GeoUtils.haversineKm(
                        location.latitude,
                        location.longitude,
                        hit.latitude,
                        hit.longitude,
                    )
                },
            )
            .firstOrNull()
    }

    private fun matchWiki(
        placeName: String,
        hits: List<WikipediaNearby>,
    ): WikipediaSummary? {
        return hits.firstOrNull { WikiTitles.titlesMatch(placeName, it.title) }?.toSummary()
    }

    private fun WikipediaNearby.toPlace(
        city: String,
        location: GeoLocation,
        activity: ActivityKind,
    ): ExplorePlace {
        return ExplorePlace(
            osmId = GeoUtils.fallbackOsmId(title, latitude, longitude),
            osmType = CoreConstants.Overpass.TYPE_NODE,
            name = title,
            latitude = latitude,
            longitude = longitude,
            activity = activity,
            distanceKm = GeoUtils.haversineKm(
                location.latitude,
                location.longitude,
                latitude,
                longitude,
            ),
            blurb = extract,
            photoUrl = imageUrl,
            countryCode = location.countryCode,
            city = city,
        )
    }

    private fun fallbackLocation(): GeoLocation {
        return GeoLocation(
            latitude = CoreConstants.Location.FALLBACK_LATITUDE,
            longitude = CoreConstants.Location.FALLBACK_LONGITUDE,
            isFallback = true,
            placeName = CoreConstants.Location.FALLBACK_CITY_NAME,
            countryCode = CoreConstants.Location.FALLBACK_COUNTRY_CODE,
        )
    }
}

/**
 * Data needed to render Home.
 *
 * @param location Active search point.
 * @param weather Current conditions, when available.
 * @param loadedActivities Chips that resolved live data for the city.
 * @param cafePlace Nearby cafe when OpenStreetMap returned one.
 * @param displayName Greeting name from settings.
 * @param useCelsius Temperature unit flag.
 * @author udit
 */
data class HomeSnapshot(
    val location: GeoLocation,
    val weather: CurrentWeather?,
    val cityPlace: ExplorePlace?,
    val activityPlaces: Map<ActivityKind, ExplorePlace> = emptyMap(),
    val cafePlace: ExplorePlace? = null,
    val loadedActivities: Set<ActivityKind> = emptySet(),
    val displayName: String,
    val useCelsius: Boolean,
) {
    /**
     * Featured row for [activity], falling back to the city overview.
     *
     * @param activity Selected chip or place overview.
     * @return Enriched place for the hero circle.
     * @author udit
     */
    fun featuredFor(activity: ActivityKind): ExplorePlace? {
        if (activity == ActivityKind.PLACE || activity == ActivityKind.CAFE) return cityPlace
        return activityPlaces[activity] ?: cityPlace
    }
}

private data class FeaturedResolution(
    val cityPlace: ExplorePlace,
    val activityPlaces: Map<ActivityKind, ExplorePlace>,
    val cafePlace: ExplorePlace?,
    val loadedActivities: Set<ActivityKind>,
)
