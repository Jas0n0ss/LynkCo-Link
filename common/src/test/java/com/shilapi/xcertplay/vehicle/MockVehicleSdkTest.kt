package com.shilapi.xcertplay.vehicle

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockVehicleSdkTest {
    @Test fun parkedScenarioStartsWithClimateOnAndMediaPaused() {
        val sdk = MockVehicleSdk(MockScenario.PARKED)
        assertTrue(sdk.state.value.hvac.power)
        assertEquals(22f, sdk.state.value.hvac.temperatureC)
        assertFalse(sdk.state.value.media.playing)
    }

    @Test fun climateAndMediaCanBeControlled() = runBlocking {
        val sdk = MockVehicleSdk(MockScenario.PARKED)
        sdk.setHvacPower(true)
        sdk.setHvacTemperature(25.5f)
        sdk.setHvacFan(6)
        sdk.play()
        sdk.next()
        assertEquals(25.5f, sdk.state.value.hvac.temperatureC)
        assertEquals(6, sdk.state.value.hvac.fanLevel)
        assertTrue(sdk.state.value.media.playing)
        assertEquals("Next track", sdk.state.value.media.title)
    }

    @Test fun drivingScenarioProvidesSpeedAndPlayingMedia() {
        val sdk = MockVehicleSdk(MockScenario.DRIVING)
        assertEquals(48, sdk.state.value.speedKmh)
        assertEquals(Gear.D, sdk.state.value.gear)
        assertTrue(sdk.state.value.media.playing)
    }

    @Test fun temperatureIsClampedToVehicleRange() = runBlocking {
        val sdk = MockVehicleSdk(MockScenario.PARKED)
        sdk.setHvacTemperature(40f)
        assertEquals(30f, sdk.state.value.hvac.temperatureC)
        sdk.setHvacTemperature(10f)
        assertEquals(16f, sdk.state.value.hvac.temperatureC)
    }
}
