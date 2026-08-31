package com.roamio.core.photo

import com.roamio.core.constants.CoreConstants
import com.roamio.core.wiki.WikiTitles
import com.roamio.core.wiki.WikipediaClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves a card photo for a named place. Always returns a loadable URL.
 *
 * Prefers the place page, then Commons/Openverse/Pexels searches that
 * include both the place and the activity so hiking, kayaking, and
 * biking do not share a generic city skyline.
 *
 * @param httpClient Shared Ktor client from core.
 * @param wikipediaClient Wikipedia and Commons lookups.
 * @param pexelsApiKey Optional Pexels key from local.properties.
 * @author udit
 */
class PlacePhotoResolver(
    private val httpClient: HttpClient,
    private val wikipediaClient: WikipediaClient,
    private val pexelsApiKey: String,
) {
    private val cache = ConcurrentHashMap<String, PlacePhoto>()

    /**
     * Returns a cached or freshly resolved photo for the place.
     *
     * @param placeName OSM or city name.
     * @param city Nearby city used for stock search.
     * @param activity Hiking, kayaking, biking, or cafe label.
     * @param osmImage Direct OSM image URL when tagged.
     * @param wikimediaFile OSM wikimedia_commons file name.
     * @param latitude Place latitude used for Wikipedia geosearch.
     * @param longitude Place longitude used for Wikipedia geosearch.
     * @return Photo URL and optional attribution.
     * @author udit
     */
    suspend fun resolve(
        placeName: String,
        city: String,
        activity: String,
        osmImage: String?,
        wikimediaFile: String?,
        latitude: Double? = null,
        longitude: Double? = null,
    ): PlacePhoto {
        val key = listOf(
            placeName,
            city,
            activity,
            osmImage.orEmpty(),
            wikimediaFile.orEmpty(),
            latitude?.toString().orEmpty(),
            longitude?.toString().orEmpty(),
        ).joinToString(CoreConstants.Logging.CACHE_KEY_SEPARATOR)
        cache[key]?.let { return it }
        val resolved = resolveUncached(
            placeName,
            city,
            activity,
            osmImage,
            wikimediaFile,
            latitude,
            longitude,
        )
        cache[key] = resolved
        return resolved
    }

    private suspend fun resolveUncached(
        placeName: String,
        city: String,
        activity: String,
        osmImage: String?,
        wikimediaFile: String?,
        latitude: Double?,
        longitude: Double?,
    ): PlacePhoto {
        if (
            !osmImage.isNullOrBlank() &&
            osmImage.startsWith(CoreConstants.Api.UPLOAD_WIKIMEDIA_PREFIX)
        ) {
            return photo(osmImage)
        }
        if (!osmImage.isNullOrBlank() && osmImage.startsWith("http")) {
            return photo(osmImage)
        }
        wikimediaUrl(wikimediaFile)?.let { url ->
            return photo(url)
        }
        val topic = topicTitle(activity)
        val queries = listOf(
            listOf(placeName, topic).query(),
            placeName,
        ).filter { it.isNotBlank() }.distinct()
        queries.forEach { query ->
            wikipediaClient.image(query)?.let { return photo(it) }
            wikipediaClient.searchImage(query)?.let { return photo(it) }
            wikipediaClient.commonsImage(query)?.let { return photo(it) }
        }
        if (latitude != null && longitude != null) {
            nearbyPlaceImage(placeName, latitude, longitude)?.let { return photo(it) }
        }
        queries.forEach { query ->
            openversePhoto(query)?.let { return it }
        }
        if (pexelsApiKey.isNotBlank()) {
            pexelsPhoto(listOf(placeName.ifBlank { city }, topic).query())?.let { return it }
        }
        return photo(fallbackFileUrl(activity))
    }

    private suspend fun nearbyPlaceImage(
        placeName: String,
        latitude: Double,
        longitude: Double,
    ): String? {
        return wikipediaClient.nearbySummaries(latitude, longitude)
            .firstOrNull { hit ->
                !hit.imageUrl.isNullOrBlank() && WikiTitles.titlesMatch(placeName, hit.title)
            }
            ?.imageUrl
    }

    private fun topicTitle(activity: String): String {
        val value = activity.lowercase(Locale.US)
        return when {
            value.contains(CoreConstants.Api.ACTIVITY_KAYAK) ||
                value.contains(CoreConstants.Api.ACTIVITY_WATER) ->
                CoreConstants.Api.PHOTO_TOPIC_KAYAKING
            value.contains(CoreConstants.Api.ACTIVITY_BIKE) ||
                value.contains(CoreConstants.Api.ACTIVITY_CYCL) ->
                CoreConstants.Api.PHOTO_TOPIC_BIKING
            value.contains(CoreConstants.Api.ACTIVITY_CAFE) ||
                value.contains(CoreConstants.Api.ACTIVITY_FOOD) ->
                CoreConstants.Api.PHOTO_TOPIC_CAFE
            value.contains(CoreConstants.Api.ACTIVITY_PLACE) -> ""
            else -> CoreConstants.Api.PHOTO_TOPIC_HIKING
        }
    }

    private fun List<String>.query(): String {
        return filter { it.isNotBlank() }.joinToString(CoreConstants.Logging.SINGLE_SPACE)
    }

    private fun fallbackFileUrl(activity: String): String {
        val value = activity.lowercase(Locale.US)
        val file = when {
            value.contains(CoreConstants.Api.ACTIVITY_KAYAK) ||
                value.contains(CoreConstants.Api.ACTIVITY_WATER) ->
                CoreConstants.Api.FALLBACK_KAYAKING_FILE
            value.contains(CoreConstants.Api.ACTIVITY_BIKE) ||
                value.contains(CoreConstants.Api.ACTIVITY_CYCL) ->
                CoreConstants.Api.FALLBACK_BIKING_FILE
            value.contains(CoreConstants.Api.ACTIVITY_CAFE) ||
                value.contains(CoreConstants.Api.ACTIVITY_FOOD) ->
                CoreConstants.Api.FALLBACK_CAFE_FILE
            value.contains(CoreConstants.Api.ACTIVITY_PLACE) ->
                CoreConstants.Api.FALLBACK_CITY_FILE
            else -> CoreConstants.Api.FALLBACK_HIKING_FILE
        }
        return CoreConstants.Api.WIKIMEDIA_FILE_PATH + file
    }

    private fun wikimediaUrl(fileName: String?): String? {
        val raw = fileName?.trim().orEmpty()
        if (raw.isEmpty()) return null
        val file = raw.removePrefix(CoreConstants.Overpass.WIKIMEDIA_FILE_PREFIX)
        return CoreConstants.Api.WIKIMEDIA_FILE_PATH + file
    }

    private fun photo(url: String, attribution: String? = null): PlacePhoto {
        return PlacePhoto(url = PhotoUrls.display(url), attribution = attribution)
    }

    private suspend fun openversePhoto(query: String): PlacePhoto? {
        return try {
            val dto: OpenverseSearchDto = httpClient.get(
                CoreConstants.Api.OPENVERSE_BASE_URL + CoreConstants.Api.OPENVERSE_IMAGES_PATH,
            ) {
                parameter(CoreConstants.Api.PARAM_Q, query)
                parameter(CoreConstants.Api.PARAM_PAGE_SIZE, CoreConstants.Api.OPENVERSE_PAGE_SIZE)
            }.body()
            val hit = dto.results.firstOrNull() ?: return null
            val url = hit.url ?: hit.thumbnail ?: return null
            photo(url, hit.creator)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun pexelsPhoto(query: String): PlacePhoto? {
        return try {
            val dto: PexelsSearchDto = httpClient.get(
                CoreConstants.Api.PEXELS_BASE_URL + CoreConstants.Api.PEXELS_SEARCH_PATH,
            ) {
                header(CoreConstants.Api.HEADER_AUTHORIZATION, pexelsApiKey)
                parameter(CoreConstants.Api.PARAM_QUERY, query)
                parameter(CoreConstants.Api.PARAM_PER_PAGE, CoreConstants.Api.PEXELS_PER_PAGE)
            }.body()
            val photo = dto.photos.firstOrNull() ?: return null
            val url = photo.src?.large ?: photo.src?.medium ?: return null
            photo(url, photo.photographer)
        } catch (_: Exception) {
            null
        }
    }
}
