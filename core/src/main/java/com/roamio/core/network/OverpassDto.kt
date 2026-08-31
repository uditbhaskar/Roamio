package com.roamio.core.network

import kotlinx.serialization.Serializable

/**
 * Overpass interpreter JSON envelope.
 *
 * @author udit
 */
@Serializable
data class OverpassResponseDto(
    val elements: List<OverpassElementDto> = emptyList(),
)

/**
 * One Overpass node, way, or relation.
 *
 * @author udit
 */
@Serializable
data class OverpassElementDto(
    val type: String,
    val id: Long,
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OverpassCenterDto? = null,
    val tags: Map<String, String> = emptyMap(),
)

/**
 * Center point returned for ways and relations.
 *
 * @author udit
 */
@Serializable
data class OverpassCenterDto(
    val lat: Double,
    val lon: Double,
)
