package com.roamio

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test confirming the app package name on a device.
 *
 * @author udit
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    /**
     * Verifies the instrumentation target uses the Roamio application ID.
     *
     * @author udit
     */
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.roamio", appContext.packageName)
    }
}
