package com.roamio.core.wiki

import com.roamio.core.places.ActivityKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for activity and place-title matching.
 *
 * @author udit
 */
class WikiTitlesTest {

    /**
     * Verifies a peak page scores for hiking, not kayaking.
     *
     * @author udit
     */
    @Test
    fun activityScore_peakMatchesHiking() {
        val title = "Nordkette"
        val extract = "A mountain ridge and hiking area above Innsbruck."
        assertTrue(WikiTitles.activityScore(title, extract, ActivityKind.HIKING) > 0)
        assertEquals(0, WikiTitles.activityScore(title, extract, ActivityKind.KAYAKING))
    }

    /**
     * Verifies a lake page scores for kayaking.
     *
     * @author udit
     */
    @Test
    fun activityScore_lakeMatchesKayaking() {
        val extract = "Lake Louise is a glacial lake used for canoe trips."
        assertTrue(WikiTitles.activityScore("Lake Louise", extract, ActivityKind.KAYAKING) > 0)
        assertEquals(0, WikiTitles.activityScore("Lake Louise", extract, ActivityKind.BIKING))
    }

    /**
     * Verifies a cycle route scores for biking.
     *
     * @author udit
     */
    @Test
    fun activityScore_cycleRouteMatchesBiking() {
        val extract = "The Innradweg is a long-distance bicycle route along the Inn."
        assertTrue(WikiTitles.activityScore("Innradweg", extract, ActivityKind.BIKING) > 0)
    }

    /**
     * Verifies a city Wikipedia page is not treated as the trail photo.
     *
     * @author udit
     */
    @Test
    fun summaryFitsPlace_rejectsCityOnlyPage() {
        val cityPage = WikipediaSummary(
            extract = "Innsbruck is the capital of Tyrol in Austria.",
            imageUrl = "https://example.com/city.jpg",
            title = "Innsbruck",
        )
        assertFalse(WikiTitles.summaryFitsPlace(cityPage, "Nordkette"))
        assertTrue(WikiTitles.summaryFits(cityPage, "Nordkette", "Innsbruck"))
    }

    /**
     * Verifies rental and shop names are skipped.
     *
     * @author udit
     */
    @Test
    fun isSkipName_dropsRentals() {
        assertTrue(WikiTitles.isSkipName("Citybike rental"))
        assertFalse(WikiTitles.isSkipName("Innradweg"))
    }

    /**
     * Verifies short river names do not match the parent city.
     *
     * @author udit
     */
    @Test
    fun titlesMatch_doesNotTreatInnAsInnsbruck() {
        assertFalse(WikiTitles.titlesMatch("Inn", "Innsbruck"))
        assertTrue(WikiTitles.titlesMatch("Inn", "Inn (river)"))
        assertTrue(WikiTitles.titlesMatch("Nordkette", "Nordkette"))
    }
}
