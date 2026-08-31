package com.roamio.core.currency

import com.roamio.core.constants.CoreConstants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Converts amounts with European Central Bank rates via Frankfurter.
 *
 * @param httpClient Shared Ktor client from core.
 * @author udit
 */
class CurrencyClient(
    private val httpClient: HttpClient,
) {

    /**
     * Converts [amount] from [from] into [to].
     *
     * @param amount Value in the source currency.
     * @param from Source currency code.
     * @param to Destination currency code.
     * @return Converted amount.
     * @author udit
     */
    suspend fun convert(amount: Double, from: String, to: String): Double {
        if (from == to) return amount
        val dto: FrankfurterLatestDto = httpClient.get(
            CoreConstants.Api.FRANKFURTER_BASE_URL + CoreConstants.Api.FRANKFURTER_LATEST_PATH,
        ) {
            parameter(CoreConstants.Api.PARAM_AMOUNT, amount)
            parameter(CoreConstants.Api.PARAM_FROM, from)
            parameter(CoreConstants.Api.PARAM_TO, to)
        }.body()
        return dto.rates[to] ?: amount
    }
}
