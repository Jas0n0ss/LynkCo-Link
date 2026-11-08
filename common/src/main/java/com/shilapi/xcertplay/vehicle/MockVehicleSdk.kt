package com.shilapi.xcertplay.vehicle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class MockScenario(val label: String) {
    PARKED("Parked"),
    DRIVING("Driving"),
    CHARGING("Charging"),
}

/** Deterministic vehicle data for local UI and repository tests. */
class MockVehicleSdk(scenario: MockScenario = MockScenario.PARKED) : VehicleSdk {
    private val _state = MutableStateFlow(initialState(scenario))
    override val state: StateFlow<VehicleState> = _state.asStateFlow()

    override suspend fun setHvacPower(on: Boolean) {
        _state.update { it.copy(hvac = it.hvac.copy(power = on)) }
    }

    override suspend fun setHvacTemperature(celsius: Float) {
        val clamped = celsius.coerceIn(16f, 30f)
        _state.update { it.copy(hvac = it.hvac.copy(temperatureC = clamped)) }
    }

    override suspend fun setHvacFan(level: Int) {
        _state.update { it.copy(hvac = it.hvac.copy(fanLevel = level.coerceIn(0, 7))) }
    }

    override suspend fun play() {
        _state.update { it.copy(media = it.media.copy(playing = true)) }
    }

    override suspend fun pause() {
        _state.update { it.copy(media = it.media.copy(playing = false)) }
    }

    override suspend fun next() {
        _state.update { it.copy(media = it.media.copy(title = "Next track", artist = "Mock artist")) }
    }

    override suspend fun previous() {
        _state.update { it.copy(media = it.media.copy(title = "Previous track", artist = "Mock artist")) }
    }

    private fun initialState(scenario: MockScenario): VehicleState = when (scenario) {
        MockScenario.PARKED -> VehicleState(
            available = true,
            speedKmh = 0,
            gear = Gear.P,
            hvac = HvacState(power = true, temperatureC = 22f, fanLevel = 2),
            media = MediaState(playing = false, title = "Mock radio", artist = "OSNPlay"),
        )
        MockScenario.DRIVING -> VehicleState(
            available = true,
            speedKmh = 48,
            gear = Gear.D,
            hvac = HvacState(power = true, temperatureC = 21f, fanLevel = 3),
            media = MediaState(playing = true, title = "Mock driving mix", artist = "OSNPlay"),
        )
        MockScenario.CHARGING -> VehicleState(
            available = true,
            speedKmh = 0,
            gear = Gear.P,
            hvac = HvacState(power = false, temperatureC = 23f, fanLevel = 0),
            media = MediaState(playing = false, title = "", artist = ""),
        )
    }
}
