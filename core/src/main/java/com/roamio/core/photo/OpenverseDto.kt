package com.roamio.core.photo

import kotlinx.serialization.Serializable

/**
 * Openverse image search envelope.
 *
 * @author udit
 */
@Serializable
data class OpenverseSearchDto(
    val results: List<OpenverseResultDto> = emptyList(),
)

/**
 * One Openverse Creative Commons image.
 *
 * @author udit
 */
@Serializable
data class OpenverseResultDto(
    val url: String? = null,
    val thumbnail: String? = null,
    val creator: String? = null,
)
