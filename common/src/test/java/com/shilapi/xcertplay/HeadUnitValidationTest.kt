package com.shilapi.xcertplay

import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class HeadUnitValidationTest {
    @Test fun reportCoversTheAdaptationSurface() {
        val report = HeadUnitValidation.report(org.robolectric.RuntimeEnvironment.getApplication())
        assertTrue(report, report.contains("Head-unit validation"))
        assertTrue(report, report.contains("lynk_model"))
        assertTrue(report, report.contains("display"))
        assertTrue(report, report.contains("cluster"))
        assertTrue(report, report.contains("audio_output"))
        assertTrue(report, report.contains("wheel_keys"))
        assertTrue(report, report.contains("carplay_assets"))
    }

    @Test fun checksHaveStableIdsAndReadableDetails() {
        val checks = HeadUnitValidation.capture(org.robolectric.RuntimeEnvironment.getApplication())
        assertTrue(checks.size >= 10)
        assertTrue(checks.map { it.id }.distinct().size == checks.size)
        assertTrue(checks.all { it.label.isNotBlank() && it.detail.isNotBlank() })
    }
}
