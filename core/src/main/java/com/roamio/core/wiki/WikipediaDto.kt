package com.roamio.core.wiki

import kotlinx.serialization.Serializable

/**
 * Wikipedia query envelope.
 *
 * @author udit
 */
@Serializable
data class WikipediaQueryDto(
    val query: WikipediaQueryInnerDto? = null,
)

/**
 * Page map from a Wikipedia query.
 *
 * @author udit
 */
@Serializable
data class WikipediaQueryInnerDto(
    val pages: Map<String, WikipediaPageDto> = emptyMap(),
)

/**
 * One Wikipedia page extract and optional thumbnail.
 *
 * @author udit
 */
@Serializable
data class WikipediaPageDto(
    val title: String? = null,
    val extract: String? = null,
    val thumbnail: WikipediaImageDto? = null,
    val missing: String? = null,
    val imageinfo: List<WikipediaImageInfoDto> = emptyList(),
    val coordinates: List<WikipediaCoordinateDto> = emptyList(),
)

/**
 * Wikipedia page coordinate from geo search.
 *
 * @param lat Latitude in decimal degrees.
 * @param lon Longitude in decimal degrees.
 * @author udit
 */
@Serializable
data class WikipediaCoordinateDto(
    val lat: Double? = null,
    val lon: Double? = null,
)

/**
 * Commons / Wikipedia image info URL pair.
 *
 * @author udit
 */
@Serializable
data class WikipediaImageInfoDto(
    val url: String? = null,
    val thumburl: String? = null,
)

/**
 * Wikipedia thumbnail URL.
 *
 * @author udit
 */
@Serializable
data class WikipediaImageDto(
    val source: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)

/**
 * REST summary payload used for a reliable thumbnail and extract.
 *
 * @author udit
 */
@Serializable
data class WikipediaRestSummaryDto(
    val title: String? = null,
    val extract: String? = null,
    val thumbnail: WikipediaImageDto? = null,
    val originalimage: WikipediaImageDto? = null,
)

/**
 * Short extract plus optional page image.
 *
 * @param extract Plain-text summary.
 * @param imageUrl Thumbnail URL when present.
 * @param title Canonical page title when the API returned one.
 * @author udit
 */
data class WikipediaSummary(
    val extract: String,
    val imageUrl: String?,
    val title: String = "",
)

/**
 * A Wikipedia page near a coordinate, used to pick real activity places.
 *
 * @param title Page title.
 * @param extract Plain-text summary.
 * @param imageUrl Lead image when present.
 * @param latitude Page coordinate latitude.
 * @param longitude Page coordinate longitude.
 * @author udit
 */
data class WikipediaNearby(
    val title: String,
    val extract: String,
    val imageUrl: String?,
    val latitude: Double,
    val longitude: Double,
) {
    /**
     * Converts this hit to a summary for extract and photo reuse.
     *
     * @return Title, extract, and image.
     * @author udit
     */
    fun toSummary(): WikipediaSummary {
        return WikipediaSummary(extract = extract, imageUrl = imageUrl, title = title)
    }
}
