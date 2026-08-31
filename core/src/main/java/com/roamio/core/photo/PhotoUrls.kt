package com.roamio.core.photo

import com.roamio.core.constants.CoreConstants
import com.roamio.core.wiki.WikipediaImageDto

/**
 * Picks a Wikimedia image size that stays sharp on a phone without a huge download.
 *
 * @author udit
 */
object PhotoUrls {

    /**
     * Rewrites a Commons thumb URL to the display width used across cards and heroes.
     *
     * @param url Remote image URL.
     * @return The same URL, or a larger Commons thumb when the source was a tiny preview.
     * @author udit
     */
    fun display(url: String): String {
        return sized(normalizeCommonsPath(url), CoreConstants.Api.PHOTO_DISPLAY_PX)
    }

    private fun normalizeCommonsPath(url: String): String {
        val prefix = CoreConstants.Api.WIKIMEDIA_FILE_PATH
        if (!url.startsWith(prefix)) return url
        val file = url.removePrefix(prefix).substringBefore('?').substringBefore('#')
        if (file.isBlank()) return url
        return "$prefix$file?width=${CoreConstants.Api.PHOTO_DISPLAY_PX}"
    }

    /**
     * Chooses a REST summary image, preferring a Commons thumb that Coil can load.
     *
     * @param thumbnail REST thumbnail, usually 320 px.
     * @param original Full lead image when Wikipedia published one.
     * @return Balanced URL, or null when neither source has a URL.
     * @author udit
     */
    fun pick(thumbnail: WikipediaImageDto?, original: WikipediaImageDto?): String? {
        val thumbUrl = thumbnail?.source?.let { display(it) }
        if (thumbUrl != null) return thumbUrl
        return original?.source?.let { display(it) }
    }

    private fun sized(url: String, px: Int): String {
        return CoreConstants.Api.WIKI_THUMB_PX_REGEX.toRegex().replace(url) { match ->
            val current = match.groupValues.getOrNull(1)?.toIntOrNull() ?: return@replace match.value
            if (current >= px) match.value else "/${px}px-"
        }
    }
}
