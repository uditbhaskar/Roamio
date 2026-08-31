package com.roamio.feature.home.currency.data

import com.roamio.core.currency.CurrencyClient
import com.roamio.core.result.AppResult
import com.roamio.core.result.runAppCatching

/**
 * Converts amounts through Frankfurter.
 *
 * @param currencyClient Frankfurter client from core.
 * @author udit
 */
class CurrencyRepository(
    private val currencyClient: CurrencyClient,
) {

    /**
     * Converts [amount] from [from] into [to].
     *
     * @param amount Source amount.
     * @param from Source currency code.
     * @param to Destination currency code.
     * @return Converted value or an error.
     * @author udit
     */
    suspend fun convert(amount: Double, from: String, to: String): AppResult<Double> {
        return runAppCatching { currencyClient.convert(amount, from, to) }
    }
}
