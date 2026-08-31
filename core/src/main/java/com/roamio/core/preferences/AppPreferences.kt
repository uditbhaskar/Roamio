package com.roamio.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.roamio.core.constants.CoreConstants
import com.roamio.core.places.ActivityKind
import com.roamio.core.places.ExplorePlace
import com.roamio.core.places.SavedPlaceRecord
import com.roamio.core.util.GeoUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = CoreConstants.Preferences.DATASTORE_NAME,
)

/**
 * App-wide DataStore for onboarding, saved places, and traveler settings.
 *
 * @param context Application context used to access DataStore.
 * @author udit
 */
class AppPreferences(
    private val context: Context,
) {
    private val onboardingKey =
        booleanPreferencesKey(CoreConstants.Preferences.KEY_ONBOARDING_COMPLETED)
    private val savedKey = stringPreferencesKey(CoreConstants.Preferences.KEY_SAVED_PLACES)
    private val nameKey = stringPreferencesKey(CoreConstants.Preferences.KEY_DISPLAY_NAME)
    private val celsiusKey = booleanPreferencesKey(CoreConstants.Preferences.KEY_USE_CELSIUS)
    private val currencyKey = stringPreferencesKey(CoreConstants.Preferences.KEY_HOME_CURRENCY)
    private val json = Json { ignoreUnknownKeys = true }
    private val savedListSerializer = ListSerializer(SavedPlaceRecord.serializer())

    val displayName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[nameKey].orEmpty()
    }

    val useCelsius: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[celsiusKey] ?: true
    }

    val homeCurrency: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[currencyKey] ?: CoreConstants.Preferences.DEFAULT_HOME_CURRENCY
    }

    val savedPlaces: Flow<List<SavedPlaceRecord>> = context.dataStore.data.map { prefs ->
        decodeSaved(prefs[savedKey])
    }

    /**
     * Persists onboarding completion state.
     *
     * @param completed True when the user has finished onboarding.
     * @author udit
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[onboardingKey] = completed
        }
    }

    /**
     * Stores the greeting display name.
     *
     * @param name Name shown after "Hi,".
     * @author udit
     */
    suspend fun setDisplayName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[nameKey] = name
        }
    }

    /**
     * Stores the temperature unit preference.
     *
     * @param celsius True for Celsius, false for Fahrenheit.
     * @author udit
     */
    suspend fun setUseCelsius(celsius: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[celsiusKey] = celsius
        }
    }

    /**
     * Stores the default Frankfurter source currency.
     *
     * @param code ISO currency code.
     * @author udit
     */
    suspend fun setHomeCurrency(code: String) {
        context.dataStore.edit { prefs ->
            prefs[currencyKey] = code
        }
    }

    /**
     * Adds or removes [place] from the local saved list.
     *
     * @param place Place to toggle.
     * @return True when the place is saved after the toggle.
     * @author udit
     */
    suspend fun toggleSaved(place: ExplorePlace): Boolean {
        var nowSaved = false
        context.dataStore.edit { prefs ->
            val current = decodeSaved(prefs[savedKey]).toMutableList()
            val exists = current.any { GeoUtils.sameSavedPlace(place, it) }
            if (exists) {
                current.removeAll { GeoUtils.sameSavedPlace(place, it) }
                nowSaved = false
            } else {
                current.add(place.toRecord())
                nowSaved = true
            }
            prefs[savedKey] = json.encodeToString(savedListSerializer, current.toList())
        }
        return nowSaved
    }

    /**
     * Whether [place] is already starred.
     *
     * @param place Place to check.
     * @return True when saved.
     * @author udit
     */
    suspend fun isSaved(place: ExplorePlace): Boolean {
        return decodeSaved(context.dataStore.data.first()[savedKey])
            .any { GeoUtils.sameSavedPlace(place, it) }
    }

    /**
     * Removes one starred [record] from the local saved list.
     *
     * @param record Saved row to delete.
     * @author udit
     */
    suspend fun removeSaved(record: SavedPlaceRecord) {
        context.dataStore.edit { prefs ->
            val current = decodeSaved(prefs[savedKey]).toMutableList()
            current.removeAll { GeoUtils.sameSavedPlace(record.toExplorePlace(), it) }
            prefs[savedKey] = json.encodeToString(savedListSerializer, current.toList())
        }
    }

    private fun decodeSaved(raw: String?): List<SavedPlaceRecord> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(savedListSerializer, raw)
        } catch (_: Exception) {
            try {
                listOf(json.decodeFromString(SavedPlaceRecord.serializer(), raw))
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}

private fun ExplorePlace.toRecord(): SavedPlaceRecord {
    return SavedPlaceRecord(
        osmId = osmId,
        osmType = osmType,
        name = name,
        latitude = latitude,
        longitude = longitude,
        countryCode = countryCode,
        photoUrl = photoUrl,
        activity = activity.name,
        blurb = blurb,
        saveKey = GeoUtils.saveKey(name, osmType, osmId, latitude, longitude),
    )
}

/**
 * Rebuilds an [ExplorePlace] from a saved record.
 *
 * @return Place suitable for the detail screen.
 * @author udit
 */
fun SavedPlaceRecord.toExplorePlace(): ExplorePlace {
    return ExplorePlace(
        osmId = osmId,
        osmType = osmType,
        name = name,
        latitude = latitude,
        longitude = longitude,
        activity = runCatching { ActivityKind.valueOf(activity) }.getOrDefault(ActivityKind.PLACE),
        blurb = blurb,
        photoUrl = photoUrl,
        countryCode = countryCode,
        countryName = countryCode,
    )
}
