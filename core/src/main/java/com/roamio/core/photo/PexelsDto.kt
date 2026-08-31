package com.roamio.core.photo

import kotlinx.serialization.Serializable

/**
 * Pexels search envelope.
 *
 * @author udit
 */
@Serializable
data class PexelsSearchDto(
    val photos: List<PexelsPhotoDto> = emptyList(),
)

/**
 * One Pexels photo record.
 *
 * @author udit
 */
@Serializable
data class PexelsPhotoDto(
    val src: PexelsSrcDto? = null,
    val photographer: String? = null,
)

/**
 * Pexels image size URLs.
 *
 * @author udit
 */
@Serializable
data class PexelsSrcDto(
    val large: String? = null,
    val medium: String? = null,
)
