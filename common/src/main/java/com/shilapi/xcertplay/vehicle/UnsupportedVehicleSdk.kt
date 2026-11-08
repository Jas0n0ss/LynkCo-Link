package com.shilapi.xcertplay.vehicle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Release fallback until the Lynk vehicle Binder/CarProperty implementation is connected. */
class UnsupportedVehicleSdk : VehicleSdk {
    private val _state = MutableStateFlow(VehicleState(available = false))
    override val state: StateFlow<VehicleState> = _state.asStateFlow()

    override suspend fun setHvacPower(on: Boolean) = Unit
    override suspend fun setHvacTemperature(celsius: Float) = Unit
    override suspend fun setHvacFan(level: Int) = Unit
    override suspend fun play() = Unit
    override suspend fun pause() = Unit
    override suspend fun next() = Unit
    override suspend fun previous() = Unit
}
