package com.roamio.feature.home.data

import com.roamio.core.places.ActivityKind
import com.roamio.core.result.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Preloads the Home snapshot while onboarding is on screen.
 *
 * @param homeRepository Shared Home loader.
 * @author udit
 */
class HomeWarmup(
    private val homeRepository: HomeRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var snapshot: HomeSnapshot? = null
    private var started = false

    /**
     * Starts the initial Home load while onboarding is visible.
     *
     * @author udit
     */
    fun prepare() {
        if (started) return
        started = true
        scope.launch {
            when (val result = homeRepository.load(ActivityKind.PLACE)) {
                is AppResult.Success -> snapshot = result.data
                is AppResult.Error -> Unit
            }
        }
    }

    /**
     * Returns the cached Home snapshot for the first paint after onboarding.
     *
     * @return Warmed snapshot when preload finished successfully.
     * @author udit
     */
    fun peekSnapshot(): HomeSnapshot? = snapshot
}
