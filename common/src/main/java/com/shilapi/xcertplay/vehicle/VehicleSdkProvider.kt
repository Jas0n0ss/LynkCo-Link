package com.shilapi.xcertplay.vehicle

import android.content.Context
import android.content.pm.ApplicationInfo

object MockSettings {
    private const val PREFS = "osnplay_dev"
    private const val KEY_MOCK = "vehicle_mock_enabled"
    private const val KEY_SCENARIO = "vehicle_mock_scenario"

    fun enabled(context: Context): Boolean = prefs(context).getBoolean(KEY_MOCK, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MOCK, enabled).apply()
    }

    fun scenario(context: Context): MockScenario = runCatching {
        MockScenario.valueOf(prefs(context).getString(KEY_SCENARIO, MockScenario.PARKED.name)!!)
    }.getOrDefault(MockScenario.PARKED)

    fun setScenario(context: Context, scenario: MockScenario) {
        prefs(context).edit().putString(KEY_SCENARIO, scenario.name).apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

object VehicleSdkProvider {
    @Volatile private var cached: VehicleSdk? = null

    fun create(context: Context): VehicleSdk = cached ?: synchronized(this) {
        cached ?: build(context).also { cached = it }
    }

    /** Call after changing the mock switch so the next read builds a fresh implementation. */
    fun invalidate() {
        cached = null
    }

    private fun build(context: Context): VehicleSdk {
        val debuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        return if (debuggable && MockSettings.enabled(context)) {
            MockVehicleSdk(MockSettings.scenario(context))
        } else {
            UnsupportedVehicleSdk()
        }
    }
}
