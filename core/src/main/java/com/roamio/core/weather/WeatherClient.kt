package com.roamio.core.weather

import com.roamio.core.constants.CoreConstants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Reads current weather and elevation from Open-Meteo.
 *
 * @param httpClient Shared Ktor client from core.
 * @author udit
 */
class WeatherClient(
    private val httpClient: HttpClient,
) {

    /**
     * Fetches current temperature and weather code.
     *
     * @param latitude Location latitude.
     * @param longitude Location longitude.
     * @return Current conditions, or null when the payload is empty.
     * @author udit
     */
    suspend fun current(latitude: Double, longitude: Double): CurrentWeather? {
        val dto: OpenMeteoForecastDto = httpClient.get(
            CoreConstants.Api.OPEN_METEO_BASE_URL + CoreConstants.Api.OPEN_METEO_FORECAST_PATH,
        ) {
            parameter(CoreConstants.Api.PARAM_LATITUDE, latitude)
            parameter(CoreConstants.Api.PARAM_LONGITUDE, longitude)
            parameter(CoreConstants.Api.PARAM_CURRENT, CoreConstants.Api.CURRENT_WEATHER_FIELDS)
            parameter(CoreConstants.Api.PARAM_TIMEZONE, CoreConstants.Api.TIMEZONE_AUTO)
        }.body()
        val temperature = dto.current?.temperatureC ?: return null
        val code = dto.current.weatherCode ?: return null
        return CurrentWeather(temperatureC = temperature, weatherCode = code)
    }

    /**
     * Fetches ground elevation in meters.
     *
     * @param latitude Location latitude.
     * @param longitude Location longitude.
     * @return Rounded elevation, or null when missing.
     * @author udit
     */
    suspend fun elevationMeters(latitude: Double, longitude: Double): Int? {
        val dto: OpenMeteoElevationDto = httpClient.get(
            CoreConstants.Api.OPEN_METEO_BASE_URL + CoreConstants.Api.OPEN_METEO_ELEVATION_PATH,
        ) {
            parameter(CoreConstants.Api.PARAM_LATITUDE, latitude)
            parameter(CoreConstants.Api.PARAM_LONGITUDE, longitude)
        }.body()
        val value = dto.elevation.firstOrNull() ?: return null
        return value.toInt()
    }
}
