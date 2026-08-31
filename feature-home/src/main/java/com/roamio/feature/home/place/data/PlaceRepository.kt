package com.roamio.feature.home.place.data

import com.roamio.core.constants.CoreConstants
import com.roamio.core.location.PlaceNameResolver
import com.roamio.core.photo.PhotoUrls
import com.roamio.core.photo.PlacePhotoResolver
import com.roamio.core.places.ActivityKind
import com.roamio.core.places.ExplorePlace
import com.roamio.core.places.NearbyBite
import com.roamio.core.places.PlacesClient
import com.roamio.core.places.SavedPlaceRecord
import com.roamio.core.places.SeedCities
import com.roamio.core.places.SelectedPlaceStore
import com.roamio.core.preferences.AppPreferences
import com.roamio.core.result.AppResult
import com.roamio.core.util.GeoUtils
import com.roamio.core.result.runAppCatching
import com.roamio.core.weather.CurrentWeather
import com.roamio.core.weather.WeatherClient
import com.roamio.core.wiki.WikiTitles
import com.roamio.core.wiki.WikipediaClient
import com.roamio.core.wiki.WikipediaSummary
import com.roamio.core.wiki.WikiText
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Loads detail extras for the selected place.
 *
 * @param selectedPlaceStore Place opened from Home or Saved.
 * @param wikipediaClient Extract and page image.
 * @param weatherClient Weather overlay and elevation fallback.
 * @param placesClient Nearby food.
 * @param placeNameResolver Nominatim cafe fallback.
 * @param photoResolver Hero image.
 * @param appPreferences Saved-star persistence.
 * @author udit
 */
class PlaceRepository(
    private val selectedPlaceStore: SelectedPlaceStore,
    private val wikipediaClient: WikipediaClient,
    private val weatherClient: WeatherClient,
    private val placesClient: PlacesClient,
    private val placeNameResolver: PlaceNameResolver,
    private val photoResolver: PlacePhotoResolver,
    private val appPreferences: AppPreferences,
) {

    /**
     * Place waiting on the detail route, before extras finish.
     *
     * @return The seed from Home or Saved, or null when none is selected.
     * @author udit
     */
    fun current(): ExplorePlace? = selectedPlaceStore.place

    /**
     * Live saved list used to keep the star in sync after a toggle.
     *
     * @return DataStore flow of starred records.
     * @author udit
     */
    fun observeSaved(): Flow<List<SavedPlaceRecord>> = appPreferences.savedPlaces

    /**
     * Reads the selected place and enriches it.
     *
     * @return Detail snapshot or an error.
     * @author udit
     */
    suspend fun load(): AppResult<PlaceSnapshot> {
        return runAppCatching {
            val seed = selectedPlaceStore.place ?: throw Exception(CoreConstants.Errors.UNKNOWN)
            val detailFocus = selectedPlaceStore.takeDetailFocus()
            val includeNearbyCafe = selectedPlaceStore.takeIncludeNearbyCafe()
            val seededCafe = selectedPlaceStore.takeSeededNearbyCafe()
            val city = seed.city.ifBlank {
                seed.name.takeIf { it.isNotBlank() }.orEmpty()
            }
            val countryName = seed.countryName.ifBlank {
                GeoUtils.countryName(seed.countryCode)
            }
            val workingSeed = when {
                seed.activity == ActivityKind.CAFE -> seed
                detailFocus == ActivityKind.CAFE -> {
                    val bite = seededCafe ?: loadNearbyCafe(seed.latitude, seed.longitude)
                    bite?.toCafePlace(city, seed.countryCode, countryName) ?: seed
                }
                else -> seed
            }
            val cafeUnavailable = detailFocus == ActivityKind.CAFE &&
                workingSeed.activity != ActivityKind.CAFE
            coroutineScope {
                val wikiDeferred = async {
                    runCatching { wikipediaClient.detailSummary(workingSeed.name) }.getOrNull()
                        ?: runCatching { wikipediaClient.detailSummary(city) }.getOrNull()
                        ?: runCatching {
                            wikipediaClient.searchSummary(
                                "${workingSeed.name} ${workingSeed.activity.name.lowercase()}",
                            )?.title?.takeIf { it.isNotBlank() }?.let { title ->
                                wikipediaClient.detailSummary(title)
                            }
                        }.getOrNull()
                }
                val weatherDeferred = async {
                    runCatching {
                        weatherClient.current(workingSeed.latitude, workingSeed.longitude)
                    }.getOrNull()
                }
                val elevationDeferred = async {
                    workingSeed.elevationMeters ?: runCatching {
                        weatherClient.elevationMeters(workingSeed.latitude, workingSeed.longitude)
                    }.getOrNull()
                }
                val foodDeferred = if (includeNearbyCafe && workingSeed.activity != ActivityKind.CAFE) {
                    async { loadNearbyCafe(workingSeed.latitude, workingSeed.longitude) }
                } else {
                    null
                }
                val wikiHit = wikiDeferred.await()
                val wikiForPhoto = wikiHit?.takeIf { hit ->
                    WikiTitles.summaryFits(hit, workingSeed.name, city)
                }
                val photoUrl = resolveHeroPhoto(workingSeed, city, wikiForPhoto)
                val nearby = foodDeferred?.await()
                val extract = detailBlurb(
                    wiki = wikiHit,
                    city = city,
                    activity = workingSeed.activity,
                )
                val place = workingSeed.copy(
                    blurb = when (workingSeed.activity) {
                        ActivityKind.CAFE -> workingSeed.blurb.ifBlank { extract }
                        else -> extract
                    },
                    photoUrl = photoUrl,
                    photoAttribution = workingSeed.photoAttribution,
                    elevationMeters = elevationDeferred.await(),
                )
                selectedPlaceStore.place = place
                PlaceSnapshot(
                    place = place,
                    weather = weatherDeferred.await(),
                    nearby = nearby,
                    isSaved = appPreferences.isSaved(place),
                    detailFocus = detailFocus.takeIf { cafeUnavailable },
                    focusUnavailable = cafeUnavailable,
                )
            }
        }
    }

    /**
     * Toggles the star for [place].
     *
     * @param place Place to save or remove.
     * @return True when saved after the toggle.
     * @author udit
     */
    suspend fun toggleSaved(place: ExplorePlace): Boolean {
        return appPreferences.toggleSaved(place)
    }

    private suspend fun loadNearbyCafe(latitude: Double, longitude: Double): NearbyBite? {
        val bite = withTimeoutOrNull(CoreConstants.Api.PLACE_FOOD_TIMEOUT_MILLIS) {
            placesClient.nearbyFood(latitude, longitude)
        }.orEmpty().firstOrNull()
            ?: runCatching {
                placeNameResolver.searchCafes(latitude, longitude)
            }.getOrDefault(emptyList()).firstOrNull()
            ?: return null
        val photo = bite.photoUrl
            ?.takeIf { url ->
                url.startsWith(CoreConstants.Api.UPLOAD_WIKIMEDIA_PREFIX) ||
                    url.startsWith(CoreConstants.Api.WIKIMEDIA_FILE_PATH)
            }
            ?: (CoreConstants.Api.WIKIMEDIA_FILE_PATH + CoreConstants.Api.FALLBACK_CAFE_FILE)
        return bite.copy(photoUrl = PhotoUrls.display(photo))
    }

    private suspend fun resolveHeroPhoto(
        seed: ExplorePlace,
        city: String,
        wiki: WikipediaSummary?,
    ): String? {
        seed.photoUrl?.takeIf { it.isNotBlank() }?.let { return PhotoUrls.display(it) }
        val placePhoto = wiki
            ?.takeIf { WikiTitles.summaryFitsPlace(it, seed.name) }
            ?.imageUrl
        val resolved = placePhoto
            ?: runCatching {
                photoResolver.resolve(
                    placeName = seed.name,
                    city = city,
                    activity = seed.activity.name.lowercase(),
                    osmImage = seed.photoUrl,
                    wikimediaFile = seed.wikimediaFile,
                    latitude = seed.latitude,
                    longitude = seed.longitude,
                ).url
            }.getOrNull()
            ?: SeedCities.photoFor(city.ifBlank { seed.name }, seed.activity)
        return resolved.takeIf { it.isNotBlank() }?.let { PhotoUrls.display(it) }
    }

    private suspend fun detailBlurb(
        wiki: WikipediaSummary?,
        city: String,
        activity: ActivityKind,
    ): String {
        WikiText.detailBody(wiki?.extract.orEmpty()).takeIf { it.isNotBlank() }?.let { return it }
        val voyage = runCatching { wikipediaClient.wikivoyageSummary(city) }.getOrNull()
        WikiText.detailBody(voyage?.extract.orEmpty()).takeIf { it.isNotBlank() }?.let { return it }
        return SeedCities.genericBlurb(activity)
    }

    private fun NearbyBite.toCafePlace(
        city: String,
        countryCode: String,
        countryName: String,
    ): ExplorePlace {
        val blurb = listOf(address, cuisine).filter { it.isNotBlank() }.joinToString(" · ")
            .ifBlank { SeedCities.genericBlurb(ActivityKind.CAFE) }
        return ExplorePlace(
            osmId = GeoUtils.fallbackOsmId(name, latitude, longitude),
            osmType = CoreConstants.Overpass.TYPE_NODE,
            name = name,
            latitude = latitude,
            longitude = longitude,
            activity = ActivityKind.CAFE,
            distanceKm = walkMinutes?.let { it / 12.0 },
            blurb = blurb,
            photoUrl = photoUrl,
            countryCode = countryCode,
            countryName = countryName,
            city = city,
            isOpenHours = true,
        )
    }
}

/**
 * Detail screen payload.
 *
 * @param place Enriched place.
 * @param weather Current weather at the place.
 * @param nearby Nearest cafe or restaurant.
 * @param isSaved Whether the star is filled.
 * @author udit
 */
data class PlaceSnapshot(
    val place: ExplorePlace,
    val weather: CurrentWeather?,
    val nearby: NearbyBite?,
    val isSaved: Boolean,
    val detailFocus: ActivityKind? = null,
    val focusUnavailable: Boolean = false,
)
