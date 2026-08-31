package com.roamio.core.places

import kotlinx.serialization.Serializable

/**
 * Persisted starred place stored in DataStore.
 *
 * @author udit
 */
@Serializable
data class SavedPlaceRecord(
    val osmId: Long,
    val osmType: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val countryCode: String,
    val photoUrl: String? = null,
    val activity: String,
    val blurb: String = "",
    val saveKey: String = "",
)
