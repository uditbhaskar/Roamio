package com.roamio.core.wiki

import com.roamio.core.constants.CoreConstants
import com.roamio.core.photo.PhotoUrls
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads a short extract and page images from Wikipedia.
 *
 * @param httpClient Shared Ktor client from core.
 * @author udit
 */
class WikipediaClient(
    private val httpClient: HttpClient,
) {

    /**
     * Queries Wikipedia by page title.
     *
     * @param title Page title or place name.
     * @return Summary when the page exists and has an extract.
     * @author udit
     */
    suspend fun summary(title: String): WikipediaSummary? {
        if (title.isBlank()) return null
        val rest = restSummary(title)
        val page = if (rest == null || rest.imageUrl.isNullOrBlank() || rest.extract.isBlank()) {
            runCatching { pageByTitle(title) }.getOrNull()
        } else {
            null
        }
        val extract = rest?.extract?.ifBlank { null }
            ?: page?.extract?.trim().orEmpty()
        val image = rest?.imageUrl
            ?: page?.thumbnail?.source?.let(PhotoUrls::display)
        if (extract.isEmpty() && image.isNullOrBlank()) return null
        return WikipediaSummary(
            extract = extract,
            imageUrl = image,
            title = rest?.title?.ifBlank { null } ?: page?.title.orEmpty(),
        )
    }

    /**
     * Loads a longer plain-text extract for place detail.
     *
     * @param title Page title or place name.
     * @return Summary when the page exists and has copy or an image.
     * @author udit
     */
    suspend fun detailSummary(title: String): WikipediaSummary? {
        if (title.isBlank()) return null
        val rest = restSummary(title)
        val canonicalTitle = rest?.title?.ifBlank { null } ?: title
        fullPageExtract(canonicalTitle)?.let { body ->
            val extract = WikiText.detailBody(body)
            if (extract.isNotBlank()) {
                return WikipediaSummary(
                    extract = extract,
                    imageUrl = rest?.imageUrl,
                    title = canonicalTitle,
                )
            }
        }
        val page = runCatching { pageByTitle(canonicalTitle) }.getOrNull()
        val pageExtract = WikiText.detailBody(page?.extract.orEmpty())
        if (pageExtract.isNotBlank() || !page?.thumbnail?.source.isNullOrBlank()) {
            return WikipediaSummary(
                extract = pageExtract,
                imageUrl = page?.thumbnail?.source?.let(PhotoUrls::display) ?: rest?.imageUrl,
                title = page?.title.orEmpty().ifBlank { canonicalTitle },
            )
        }
        rest ?: return null
        return rest.copy(extract = WikiText.detailBody(rest.extract))
    }

    /**
     * Page thumbnail for an exact title, including redirects.
     *
     * @param title Page title or place name.
     * @return Image URL when the page has a lead image.
     * @author udit
     */
    suspend fun image(title: String): String? {
        if (title.isBlank()) return null
        restSummary(title)?.imageUrl?.let { return it }
        return try {
            pageByTitle(title)?.thumbnail?.source?.let(PhotoUrls::display)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * First Wikipedia page that matches [query], with extract and image.
     *
     * @param query Free-text search such as "kayaking Innsbruck".
     * @return Summary when a page has copy or a lead image.
     * @author udit
     */
    suspend fun searchSummary(query: String): WikipediaSummary? {
        if (query.isBlank()) return null
        return try {
            val dto: WikipediaQueryDto = httpClient.get(
                CoreConstants.Api.WIKIPEDIA_BASE_URL + CoreConstants.Api.WIKIPEDIA_API_PATH,
            ) {
                parameter(CoreConstants.Api.PARAM_ACTION, CoreConstants.Api.WIKI_ACTION_QUERY)
                parameter(CoreConstants.Api.PARAM_FORMAT, CoreConstants.Api.WIKI_FORMAT_JSON)
                parameter(CoreConstants.Api.PARAM_GENERATOR, CoreConstants.Api.WIKI_GENERATOR_SEARCH)
                parameter(CoreConstants.Api.PARAM_GSRSEARCH, query)
                parameter(CoreConstants.Api.PARAM_GSRLIMIT, CoreConstants.Api.WIKI_SEARCH_CANDIDATES)
                parameter(CoreConstants.Api.PARAM_PROP, CoreConstants.Api.WIKI_PROP_EXTRACTS_IMAGES)
                parameter(CoreConstants.Api.PARAM_EXPLAINTEXT, CoreConstants.Api.WIKI_EXPLAINTEXT)
                parameter(CoreConstants.Api.PARAM_EXCHARS, CoreConstants.Api.WIKI_EXCHARS)
                parameter(CoreConstants.Api.PARAM_PIPROP, CoreConstants.Api.WIKI_PIPROP_THUMB)
                parameter(CoreConstants.Api.PARAM_PITHUMBSIZE, CoreConstants.Api.WIKI_THUMB_SIZE)
            }.body()
            val page = dto.query?.pages?.values
                ?.filter { it.missing == null && WikiTitles.isPlaceTitle(it.title.orEmpty()) }
                ?.firstOrNull { page ->
                    !page.extract.isNullOrBlank() || !page.thumbnail?.source.isNullOrBlank()
                }
                ?: return null
            WikipediaSummary(
                extract = page.extract.orEmpty().trim(),
                imageUrl = page.thumbnail?.source?.let(PhotoUrls::display),
                title = page.title.orEmpty(),
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Wikipedia pages within 10 km of a coordinate, with extracts and images.
     *
     * @param latitude Search latitude.
     * @param longitude Search longitude.
     * @return Nearby place pages that have coordinates.
     * @author udit
     */
    suspend fun nearbySummaries(latitude: Double, longitude: Double): List<WikipediaNearby> {
        return try {
            val dto: WikipediaQueryDto = httpClient.get(
                CoreConstants.Api.WIKIPEDIA_BASE_URL + CoreConstants.Api.WIKIPEDIA_API_PATH,
            ) {
                parameter(CoreConstants.Api.PARAM_ACTION, CoreConstants.Api.WIKI_ACTION_QUERY)
                parameter(CoreConstants.Api.PARAM_FORMAT, CoreConstants.Api.WIKI_FORMAT_JSON)
                parameter(CoreConstants.Api.PARAM_GENERATOR, CoreConstants.Api.WIKI_GENERATOR_GEOSEARCH)
                parameter(
                    CoreConstants.Api.PARAM_GGSCOORD,
                    String.format(
                        Locale.US,
                        "%.5f%s%.5f",
                        latitude,
                        CoreConstants.Api.WIKI_COORD_SEPARATOR,
                        longitude,
                    ),
                )
                parameter(CoreConstants.Api.PARAM_GGSRADIUS, CoreConstants.Api.WIKI_GEO_RADIUS_METERS)
                parameter(CoreConstants.Api.PARAM_GGSLIMIT, CoreConstants.Api.WIKI_GEO_LIMIT)
                parameter(CoreConstants.Api.PARAM_PROP, CoreConstants.Api.WIKI_PROP_EXTRACTS_IMAGES_COORDS)
                parameter(CoreConstants.Api.PARAM_EXPLAINTEXT, CoreConstants.Api.WIKI_EXPLAINTEXT)
                parameter(CoreConstants.Api.PARAM_EXCHARS, CoreConstants.Api.WIKI_EXCHARS)
                parameter(CoreConstants.Api.PARAM_PIPROP, CoreConstants.Api.WIKI_PIPROP_THUMB)
                parameter(CoreConstants.Api.PARAM_PITHUMBSIZE, CoreConstants.Api.WIKI_THUMB_SIZE)
            }.body()
            dto.query?.pages?.values.orEmpty().mapNotNull { page ->
                if (page.missing != null) return@mapNotNull null
                val title = page.title.orEmpty()
                if (!WikiTitles.isPlaceTitle(title)) return@mapNotNull null
                val cord = page.coordinates.firstOrNull()
                val lat = cord?.lat ?: latitude
                val lon = cord?.lon ?: longitude
                val extract = page.extract.orEmpty().trim()
                val image = page.thumbnail?.source?.let(PhotoUrls::display)
                if (extract.isEmpty() && image.isNullOrBlank()) return@mapNotNull null
                WikipediaNearby(
                    title = title,
                    extract = extract,
                    imageUrl = image,
                    latitude = lat,
                    longitude = lon,
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Wiki voyage travel extract for a city or region.
     *
     * @param title City or region page title.
     * @return Travel summary when Wiki voyage has that page.
     * @author udit
     */
    suspend fun wikiVoyageSummary(title: String): WikipediaSummary? {
        return restSummaryOn(CoreConstants.Api.WIKIVOYAGE_BASE_URL, title)
    }

    /**
     * First thumbnail from a Wikipedia full-text search.
     *
     * @param query Free-text search.
     * @return Image URL when a matching page has a lead image.
     * @author udit
     */
    suspend fun searchImage(query: String): String? {
        if (query.isBlank()) return null
        return try {
            val dto: WikipediaQueryDto = httpClient.get(
                CoreConstants.Api.WIKIPEDIA_BASE_URL + CoreConstants.Api.WIKIPEDIA_API_PATH,
            ) {
                parameter(CoreConstants.Api.PARAM_ACTION, CoreConstants.Api.WIKI_ACTION_QUERY)
                parameter(CoreConstants.Api.PARAM_FORMAT, CoreConstants.Api.WIKI_FORMAT_JSON)
                parameter(CoreConstants.Api.PARAM_GENERATOR, CoreConstants.Api.WIKI_GENERATOR_SEARCH)
                parameter(CoreConstants.Api.PARAM_GSRSEARCH, query)
                parameter(CoreConstants.Api.PARAM_GSRLIMIT, CoreConstants.Api.WIKI_SEARCH_LIMIT)
                parameter(CoreConstants.Api.PARAM_PROP, CoreConstants.Api.WIKI_PROP_PAGEIMAGES)
                parameter(CoreConstants.Api.PARAM_PIPROP, CoreConstants.Api.WIKI_PIPROP_THUMB)
                parameter(CoreConstants.Api.PARAM_PITHUMBSIZE, CoreConstants.Api.WIKI_THUMB_SIZE)
            }.body()
            dto.query?.pages?.values
                ?.firstOrNull { page ->
                    page.missing == null &&
                        WikiTitles.isPlaceTitle(page.title.orEmpty()) &&
                        !page.thumbnail?.source.isNullOrBlank()
                }
                ?.thumbnail
                ?.source
                ?.let(PhotoUrls::display)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * First File-namespace image from Wikimedia Commons search.
     *
     * @param query Free-text search.
     * @return Direct Commons file URL when a file matches.
     * @author udit
     */
    suspend fun commonsImage(query: String): String? {
        if (query.isBlank()) return null
        return try {
            val dto: WikipediaQueryDto = httpClient.get(
                CoreConstants.Api.COMMONS_BASE_URL + CoreConstants.Api.WIKIPEDIA_API_PATH,
            ) {
                parameter(CoreConstants.Api.PARAM_ACTION, CoreConstants.Api.WIKI_ACTION_QUERY)
                parameter(CoreConstants.Api.PARAM_FORMAT, CoreConstants.Api.WIKI_FORMAT_JSON)
                parameter(CoreConstants.Api.PARAM_GENERATOR, CoreConstants.Api.WIKI_GENERATOR_SEARCH)
                parameter(CoreConstants.Api.PARAM_GSRSEARCH, query)
                parameter(CoreConstants.Api.PARAM_GSRNAMESPACE, CoreConstants.Api.COMMONS_FILE_NAMESPACE)
                parameter(CoreConstants.Api.PARAM_GSRLIMIT, CoreConstants.Api.WIKI_SEARCH_LIMIT)
                parameter(CoreConstants.Api.PARAM_PROP, CoreConstants.Api.WIKI_PROP_IMAGEINFO)
                parameter(CoreConstants.Api.PARAM_IIPROP, CoreConstants.Api.COMMONS_IIPROP)
                parameter(CoreConstants.Api.PARAM_IIURLWIDTH, CoreConstants.Api.WIKI_THUMB_SIZE)
            }.body()
            val info = dto.query?.pages?.values?.firstOrNull()?.imageinfo?.firstOrNull()
            val url = info?.thumburl?.ifBlank { null } ?: info?.url
            url?.let(PhotoUrls::display)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun restSummary(title: String): WikipediaSummary? {
        return restSummaryOn(CoreConstants.Api.WIKIPEDIA_BASE_URL, title)
    }

    private suspend fun fullPageExtract(title: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val dto: WikipediaQueryDto = httpClient.get(
                    CoreConstants.Api.WIKIPEDIA_BASE_URL + CoreConstants.Api.WIKIPEDIA_API_PATH,
                ) {
                    parameter(CoreConstants.Api.PARAM_ACTION, CoreConstants.Api.WIKI_ACTION_QUERY)
                    parameter(CoreConstants.Api.PARAM_FORMAT, CoreConstants.Api.WIKI_FORMAT_JSON)
                    parameter(CoreConstants.Api.PARAM_PROP, CoreConstants.Api.WIKI_PROP_EXTRACTS)
                    parameter(CoreConstants.Api.PARAM_EXPLAINTEXT, CoreConstants.Api.WIKI_EXPLAINTEXT)
                    parameter(
                        CoreConstants.Api.PARAM_EXSECTIONFORMAT,
                        CoreConstants.Api.WIKI_EXSECTIONFORMAT_PLAIN,
                    )
                    parameter(CoreConstants.Api.PARAM_REDIRECTS, CoreConstants.Api.WIKI_REDIRECTS)
                    parameter(CoreConstants.Api.PARAM_TITLES, title)
                }.body()
                dto.query?.pages?.values
                    ?.firstOrNull { it.missing == null }
                    ?.extract
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
            } catch (_: Exception) {
                null
            }
        }
    }

    private suspend fun restSummaryOn(baseUrl: String, title: String): WikipediaSummary? {
        return try {
            val encoded = withContext(Dispatchers.IO) {
                URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            }
                .replace("+", "%20")
            val dto: WikipediaRestSummaryDto = httpClient.get(
                baseUrl + CoreConstants.Api.WIKIPEDIA_REST_SUMMARY_PATH + encoded,
            ).body()
            val extract = dto.extract?.trim().orEmpty()
            val image = PhotoUrls.pick(dto.thumbnail, dto.originalimage)
            if (extract.isEmpty() && image.isNullOrBlank()) return null
            WikipediaSummary(
                extract = extract,
                imageUrl = image,
                title = dto.title.orEmpty(),
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun pageByTitle(
        title: String,
        chars: String = CoreConstants.Api.WIKI_EXCHARS,
    ): WikipediaPageDto? = withContext(Dispatchers.IO) {
        val dto: WikipediaQueryDto = httpClient.get(
            CoreConstants.Api.WIKIPEDIA_BASE_URL + CoreConstants.Api.WIKIPEDIA_API_PATH,
        ) {
            parameter(CoreConstants.Api.PARAM_ACTION, CoreConstants.Api.WIKI_ACTION_QUERY)
            parameter(CoreConstants.Api.PARAM_FORMAT, CoreConstants.Api.WIKI_FORMAT_JSON)
            parameter(CoreConstants.Api.PARAM_PROP, CoreConstants.Api.WIKI_PROP_EXTRACTS_IMAGES)
            parameter(CoreConstants.Api.PARAM_EXPLAINTEXT, CoreConstants.Api.WIKI_EXPLAINTEXT)
            parameter(CoreConstants.Api.PARAM_EXCHARS, chars)
            parameter(CoreConstants.Api.PARAM_PIPROP, CoreConstants.Api.WIKI_PIPROP_THUMB)
            parameter(CoreConstants.Api.PARAM_PITHUMBSIZE, CoreConstants.Api.WIKI_THUMB_SIZE)
            parameter(CoreConstants.Api.PARAM_REDIRECTS, CoreConstants.Api.WIKI_REDIRECTS)
            parameter(CoreConstants.Api.PARAM_TITLES, title)
        }.body()
        val page = dto.query?.pages?.values?.firstOrNull() ?: return@withContext null
        if (page.missing != null) return@withContext null
        page
    }
}
