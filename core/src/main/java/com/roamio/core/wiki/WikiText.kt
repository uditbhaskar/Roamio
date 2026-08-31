package com.roamio.core.wiki

/**
 * Normalizes Wikipedia plain-text extracts for detail screens.
 *
 * @author udit
 */
object WikiText {

    private val SECTION_HEADER = Regex("==+\\s*([^=]+?)\\s*==+")
    private val HTML_TAG = Regex("<[^>]+>")
    private val HTML_ENTITY = Regex("&([#a-zA-Z0-9]+);")

    /**
     * Cleans a Wikipedia extract for scrollable detail copy.
     *
     * @param raw Plain-text extract from Wikipedia.
     * @return Normalized body text without section markup.
     * @author udit
     */
    fun detailBody(raw: String): String {
        val normalized = if (raw.contains('<')) stripHtml(raw) else raw.trim()
        return normalized
            .lineSequence()
            .map { line -> SECTION_HEADER.replace(line.trim()) { match -> match.groupValues[1].trim() } }
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .let(::stripApiTruncation)
            .trim()
    }

    private fun stripApiTruncation(text: String): String {
        if (text.length < 1_100 || !text.endsWith("...")) return text
        return text.removeSuffix("...").trimEnd()
    }

    /**
     * Converts mobile-section HTML into plain text paragraphs.
     *
     * @param html HTML snippet from Wikipedia REST.
     * @return Plain text without tags or entities.
     * @author udit
     */
    fun stripHtml(html: String): String {
        return html
            .replace(HTML_TAG, " ")
            .replace(HTML_ENTITY, " ")
            .lineSequence()
            .map { line -> line.trim().replace(Regex("\\s+"), " ") }
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
    }
}
