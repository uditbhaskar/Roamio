package com.roamio.core.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Open-Meteo forecast payload used for the weather chip.
 *
 * @author udit
 */
@Serializable
data class OpenMeteoForecastDto(
    val current: OpenMeteoCurrentDto? = null,
)

/**
 * Current conditions from Open-Meteo.
 *
 * @author udit
 */
@Serializable
data class OpenMeteoCurrentDto(
    @SerialName("temperature_2m") val temperatureC: Double? = null,
    @SerialName("weather_code") val weatherCode: Int? = null,
)

/**
 * Open-Meteo elevation payload.
 *
 * @author udit
 */
@Serializable
data class OpenMeteoElevationDto(
    val elevation: List<Double> = emptyList(),
)

/**
 * Current temperature and WMO weather code.
 *
 * @param temperatureC Temperature in Celsius.
 * @param weatherCode Open-Meteo weather code.
 * @author udit
 */
data class CurrentWeather(
    val temperatureC: Double,
    val weatherCode: Int,
)
