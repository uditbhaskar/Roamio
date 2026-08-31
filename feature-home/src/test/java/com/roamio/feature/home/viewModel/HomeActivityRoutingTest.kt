package com.roamio.feature.home.viewModel

import com.roamio.core.places.ActivityKind
import com.roamio.core.places.ExplorePlace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Home hero merge helpers.
 *
 * @author udit
 */
class HomeActivityRoutingTest {

    private val london = ExplorePlace(
        osmId = 1L,
        osmType = "node",
        name = "Hyde Park",
        latitude = 51.51,
        longitude = -0.16,
        activity = ActivityKind.PLACE,
        photoUrl = "https://example.com/london.jpg",
        city = "London",
    )

    private val banff = ExplorePlace(
        osmId = 2L,
        osmType = "node",
        name = "Lake Louise",
        latitude = 51.42,
        longitude = -116.18,
        activity = ActivityKind.PLACE,
        photoUrl = "https://example.com/banff.jpg",
        city = "Banff",
    )

    /**
     * Verifies switching cities never keeps the previous hero photo.
     *
     * @author udit
     */
    @Test
    fun mergeFeaturedPlace_differentCity_usesIncomingPhoto() {
        val merged = mergeFeaturedPlace(london, banff)
        assertEquals("https://example.com/banff.jpg", merged.photoUrl)
    }

    /**
     * Verifies a refresh for the same place keeps the visible photo when the API omits one.
     *
     * @author udit
     */
    @Test
    fun mergeFeaturedPlace_samePlace_keepsCurrentPhotoWhenIncomingBlank() {
        val incoming = london.copy(photoUrl = null, blurb = "Updated copy.")
        val merged = mergeFeaturedPlace(london, incoming)
        assertEquals("https://example.com/london.jpg", merged.photoUrl)
        assertEquals("Updated copy.", merged.blurb)
    }

    /**
     * Verifies city and coordinates gate same-place detection.
     *
     * @author udit
     */
    @Test
    fun isSameFeaturedPlace_requiresMatchingCity() {
        val sameNameDifferentCity = london.copy(city = "Banff", latitude = 51.42, longitude = -116.18)
        assertFalse(isSameFeaturedPlace(london, sameNameDifferentCity))
        assertTrue(isSameFeaturedPlace(london, london.copy(blurb = "Refreshed.")))
    }
}
