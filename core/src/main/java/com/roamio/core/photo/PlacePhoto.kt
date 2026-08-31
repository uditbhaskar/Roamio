package com.roamio.core.photo

/**
 * Resolved hero or card image for a place.
 *
 * @param url Remote image URL that Coil can load.
 * @param attribution Credit line for Wikimedia, Openverse, or Pexels.
 * @author udit
 */
data class PlacePhoto(
    val url: String,
    val attribution: String?,
)
