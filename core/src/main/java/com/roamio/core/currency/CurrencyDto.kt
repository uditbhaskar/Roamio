package com.roamio.core.currency

import kotlinx.serialization.Serializable

/**
 * Frankfurter latest-rate payload.
 *
 * @author udit
 */
@Serializable
data class FrankfurterLatestDto(
    val amount: Double = 1.0,
    val base: String = "",
    val rates: Map<String, Double> = emptyMap(),
)
