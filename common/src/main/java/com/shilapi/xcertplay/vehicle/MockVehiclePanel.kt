package com.shilapi.xcertplay.vehicle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val MockDark = darkColorScheme(
    background = Color(0xFF0C111B),
    surface = Color(0xFF151E2C),
    primary = Color(0xFFA6C8FF),
    onPrimary = Color(0xFF0C111B),
    onBackground = Color(0xFFF1F5FC),
    onSurface = Color(0xFFF1F5FC),
)

/** Debug-only responsive surface for MockVehicleActivity; never shown in the release app. */
@Composable
fun MockVehiclePanel(scenario: MockScenario, modifier: Modifier = Modifier) {
    val sdk = remember(scenario) { MockVehicleSdk(scenario) }
    val state by sdk.state.collectAsState()
    val scope = rememberCoroutineScope()

    MaterialTheme(colorScheme = MockDark) {
        Surface(color = MockDark.background, modifier = modifier.fillMaxSize()) {
            BoxWithConstraints(Modifier.fillMaxSize().systemBarsPadding().padding(28.dp)) {
                val wide = maxWidth >= 1000.dp
                val content = @Composable {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text("Mock vehicle · ${scenario.label}", style = MaterialTheme.typography.headlineMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MockScenario.entries.forEach { item ->
                                if (item == scenario) Button(onClick = {}) { Text(item.label) }
                                else OutlinedButton(onClick = {}) { Text(item.label) }
                            }
                        }
                        if (wide) {
                            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                ClimateCard(
                                    state,
                                    onWarmer = { scope.launch { sdk.setHvacTemperature(state.hvac.temperatureC + 0.5f) } },
                                    onCooler = { scope.launch { sdk.setHvacTemperature(state.hvac.temperatureC - 0.5f) } },
                                    onFan = { scope.launch { sdk.setHvacFan(state.hvac.fanLevel + 1) } },
                                    modifier = Modifier.weight(1f),
                                )
                                MediaCard(
                                    state,
                                    onPrevious = { scope.launch { sdk.previous() } },
                                    onToggle = { scope.launch { if (state.media.playing) sdk.pause() else sdk.play() } },
                                    onNext = { scope.launch { sdk.next() } },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        } else {
                            ClimateCard(
                                state,
                                onWarmer = { scope.launch { sdk.setHvacTemperature(state.hvac.temperatureC + 0.5f) } },
                                onCooler = { scope.launch { sdk.setHvacTemperature(state.hvac.temperatureC - 0.5f) } },
                                onFan = { scope.launch { sdk.setHvacFan(state.hvac.fanLevel + 1) } },
                            )
                            MediaCard(
                                state,
                                onPrevious = { scope.launch { sdk.previous() } },
                                onToggle = { scope.launch { if (state.media.playing) sdk.pause() else sdk.play() } },
                                onNext = { scope.launch { sdk.next() } },
                            )
                        }
                        Text(
                            "Debug surface. Release uses UnsupportedVehicleSdk until the Lynk vehicle binder is connected.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { content() }
            }
        }
    }
}

@Composable
private fun ClimateCard(
    state: VehicleState,
    onWarmer: () -> Unit,
    onCooler: () -> Unit,
    onFan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier.widthIn(max = 760.dp).fillMaxWidth().height(190.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Climate", style = MaterialTheme.typography.titleLarge)
            Text("${if (state.hvac.power) "On" else "Off"} · ${state.hvac.temperatureC}°C · fan ${state.hvac.fanLevel}")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCooler) { Text("−") }
                Button(onClick = onWarmer) { Text("+") }
                OutlinedButton(onClick = onFan) { Text("Fan") }
            }
        }
    }
}

@Composable
private fun MediaCard(
    state: VehicleState,
    onPrevious: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier.widthIn(max = 760.dp).fillMaxWidth().height(190.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Media", style = MaterialTheme.typography.titleLarge)
            Text(if (state.media.title.isBlank()) "Nothing playing" else "${state.media.title} · ${state.media.artist}")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onPrevious) { Text("Previous") }
                Button(onClick = onToggle) { Text(if (state.media.playing) "Pause" else "Play") }
                OutlinedButton(onClick = onNext) { Text("Next") }
            }
        }
    }
}
