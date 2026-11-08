package com.shilapi.xcertplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.shilapi.xcertplay.vehicle.MockScenario
import com.shilapi.xcertplay.vehicle.MockVehiclePanel

/** Debug-only host for the Compose mock dashboard; never included in release builds. */
class MockVehicleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scenario = runCatching {
            MockScenario.valueOf(intent.getStringExtra("scenario") ?: MockScenario.PARKED.name)
        }.getOrDefault(MockScenario.PARKED)
        setContent { MockVehiclePanel(scenario) }
    }
}
