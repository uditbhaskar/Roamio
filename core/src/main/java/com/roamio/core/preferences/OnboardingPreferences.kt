package com.roamio.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.roamio.core.constants.CoreConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = CoreConstants.Preferences.DATASTORE_NAME,
)

/**
 * Persists app-level preferences such as onboarding completion.
 *
 * @param context Application context used to access DataStore.
 * @author udit
 */
class OnboardingPreferences(
    private val context: Context,
) {
    private val onboardingKey =
        booleanPreferencesKey(CoreConstants.Preferences.KEY_ONBOARDING_COMPLETED)

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[onboardingKey] ?: false
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
}
