package com.roamio.core.wiki

import com.roamio.core.constants.CoreConstants
import com.roamio.core.places.ActivityKind
import java.util.Locale

/**
 * Rejects Wikipedia pages that are websites or unrelated to a place.
 *
 * @author udit
 */
object WikiTitles {

    /**
     * Whether [title] can be shown as a destination name.
     *
     * @param title Wikipedia page title.
     * @return False for domains, lists, and generic activity pages.
     * @author udit
     */
    fun isPlaceTitle(title: String): Boolean {
        val lower = title.trim().lowercase(Locale.US)
        if (lower.isEmpty()) return false
        if (lower.contains(CoreConstants.Wiki.DOMAIN_DOT)) return false
        if (lower.contains(CoreConstants.Wiki.DISAMBIGUATION)) return false
        if (lower.startsWith(CoreConstants.Wiki.LIST_PREFIX)) return false
        return lower !in CoreConstants.Wiki.GENERIC_TITLES
    }

    /**
     * Whether [summary] is about [placeName] or [city], not a random article.
     *
     * @param summary Wikipedia hit.
     * @param placeName OSM or landmark name.
     * @param city Search city.
     * @return True when the title or extract mentions the place or city.
     * @author udit
     */
    fun summaryFits(summary: WikipediaSummary, placeName: String, city: String): Boolean {
        if (!isPlaceTitle(summary.title)) return false
        val haystack = listOf(summary.title, summary.extract)
            .joinToString(CoreConstants.Logging.SINGLE_SPACE)
            .lowercase(Locale.US)
        val place = placeName.trim().lowercase(Locale.US)
        val cityName = city.trim().lowercase(Locale.US)
        if (place.length >= CoreConstants.Wiki.MIN_TOKEN_LENGTH && haystack.contains(place)) {
            return true
        }
        if (cityName.length >= CoreConstants.Wiki.MIN_TOKEN_LENGTH && haystack.contains(cityName)) {
            return true
        }
        if (place.isNotEmpty() && place == summary.title.trim().lowercase(Locale.US)) {
            return true
        }
        return false
    }

    /**
     * Whether [summary] is about [placeName], not only the parent city.
     *
     * @param summary Wikipedia hit.
     * @param placeName OSM or landmark name.
     * @return True when the title or extract mentions the place.
     * @author udit
     */
    fun summaryFitsPlace(summary: WikipediaSummary, placeName: String): Boolean {
        if (!isPlaceTitle(summary.title)) return false
        return titlesMatch(placeName, summary.title) || extractMentions(summary.extract, placeName)
    }

    /**
     * How well [title] and [extract] match [activity] keywords.
     *
     * @param title Place or Wikipedia title.
     * @param extract Optional page extract.
     * @param activity Hiking, kayaking, or biking.
     * @return Count of matching activity hints.
     * @author udit
     */
    fun activityScore(title: String, extract: String, activity: ActivityKind): Int {
        val haystack = listOf(title, extract)
            .joinToString(CoreConstants.Logging.SINGLE_SPACE)
            .lowercase(Locale.US)
        return hintsFor(activity).count { haystack.contains(it) }
    }

    /**
     * Whether [name] looks like a shop or amenity, not a destination.
     *
     * @param name OSM display name.
     * @return True when the name should be skipped.
     * @author udit
     */
    fun isSkipName(name: String): Boolean {
        val lower = name.lowercase(Locale.US)
        return CoreConstants.Wiki.SKIP_NAME_HINTS.any { lower.contains(it) }
    }

    /**
     * Whether [placeName] and [wikiTitle] refer to the same place.
     *
     * @param placeName OSM or landmark name.
     * @param wikiTitle Wikipedia page title.
     * @return True when the labels align without a loose substring match.
     * @author udit
     */
    fun titlesMatch(placeName: String, wikiTitle: String): Boolean {
        val place = placeName.trim().lowercase(Locale.US)
        val title = wikiTitle.substringBefore('(').trim().lowercase(Locale.US)
        if (place.isEmpty() || title.isEmpty()) return false
        if (place == title) return true
        val min = CoreConstants.Wiki.MIN_TOKEN_LENGTH
        if (place.length >= min && (title.startsWith("$place ") || title.startsWith("$place,"))) {
            return true
        }
        if (title.length >= min && (place.startsWith("$title ") || place.startsWith("$title,"))) {
            return true
        }
        return false
    }

    private fun extractMentions(extract: String, placeName: String): Boolean {
        val place = placeName.trim().lowercase(Locale.US)
        if (place.length < CoreConstants.Wiki.MIN_TOKEN_LENGTH) return false
        return extract.lowercase(Locale.US).contains(place)
    }

    private fun hintsFor(activity: ActivityKind): List<String> {
        return when (activity) {
            ActivityKind.PLACE -> emptyList()
            ActivityKind.POPULAR,
            ActivityKind.CAFE,
            -> emptyList()
            ActivityKind.HIKING -> CoreConstants.Wiki.HIKING_HINTS
            ActivityKind.KAYAKING -> CoreConstants.Wiki.KAYAKING_HINTS
            ActivityKind.BIKING -> CoreConstants.Wiki.BIKING_HINTS
        }
    }
}
