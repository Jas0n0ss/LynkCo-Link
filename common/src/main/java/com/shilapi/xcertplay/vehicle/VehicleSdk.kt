package com.shilapi.xcertplay.vehicle

import kotlinx.coroutines.flow.StateFlow

enum class Gear { P, R, N, D }

data class HvacState(
    val power: Boolean = false,
    val temperatureC: Float = 22f,
    val fanLevel: Int = 2,
)

data class MediaState(
    val playing: Boolean = false,
    val title: String = "",
    val artist: String = "",
)

data class VehicleState(
    val available: Boolean = false,
    val speedKmh: Int = 0,
    val gear: Gear = Gear.P,
    val hvac: HvacState = HvacState(),
    val media: MediaState = MediaState(),
)

/**
 * Boundary between the app and the head-unit vehicle APIs. Real and mock implementations
 * share this contract; business code never branches on the mock mode.
 */
interface VehicleSdk {
    val state: StateFlow<VehicleState>

    suspend fun setHvacPower(on: Boolean)
    suspend fun setHvacTemperature(celsius: Float)
    suspend fun setHvacFan(level: Int)

    suspend fun play()
    suspend fun pause()
    suspend fun next()
    suspend fun previous()
}
