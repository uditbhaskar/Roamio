package com.roamio.core.wiki

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [WikiText].
 *
 * @author udit
 */
class WikiTextTest {

    /**
     * Verifies section headers are flattened for detail copy.
     *
     * @author udit
     */
    @Test
    fun detailBody_stripsSectionHeaders() {
        val raw = "Gangtok is a city.\n\n== Etymology ==\n\nThe name is unclear, though the local story continues."
        val body = WikiText.detailBody(raw)
        assertFalse(body.contains("=="))
        assertEquals(
            "Gangtok is a city.\n\nEtymology\n\nThe name is unclear, though the local story continues.",
            body,
        )
    }

    /**
     * Verifies mobile-section HTML is flattened to plain text.
     *
     * @author udit
     */
    @Test
    fun stripHtml_removesTags() {
        val raw = "<p>Gangtok is the capital of <b>Sikkim</b>.</p>"
        assertTrue(WikiText.stripHtml(raw).contains("Gangtok is the capital of Sikkim"))
    }

    /**
     * Verifies API truncation markers are removed from long extracts.
     *
     * @author udit
     */
    @Test
    fun detailBody_stripsApiTruncationSuffix() {
        val truncated = "A".repeat(1_150) + "..."
        val body = WikiText.detailBody(truncated)
        assertFalse(body.endsWith("..."))
        assertEquals(1_150, body.length)
    }
}
