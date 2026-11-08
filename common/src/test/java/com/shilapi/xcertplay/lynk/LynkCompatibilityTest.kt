package com.shilapi.xcertplay.lynk

import org.junit.Assert.assertEquals
import org.junit.Test

class LynkCompatibilityTest {
    @Test fun lynk01On8155Osn2IsPrimary() {
        val profile = LynkCompatibility.detect(
            "Lynk & Co", "Lynk", "Lynk & Co 01", "osn", "Lynk/OSN2/01", "OSN 2.0",
            "SA8155P", 29,
        )
        assertEquals(LynkModel.LYNK_01, profile.model)
        assertEquals(LynkPlatform.SNAPDRAGON_8155, profile.platform)
        assertEquals(LynkGeneration.OSN_2, profile.generation)
        assertEquals(LynkSupport.PRIMARY, profile.support)
    }

    @Test fun lynk03On820AIsCompatible() {
        val profile = LynkCompatibility.detect(
            "Lynk & Co", "Lynk", "Lynk & Co 03", "osn", "Lynk/OSN/03", "OSN",
            "MSM8998", 28,
        )
        assertEquals(LynkModel.LYNK_03, profile.model)
        assertEquals(LynkPlatform.SNAPDRAGON_820A, profile.platform)
        assertEquals(LynkSupport.COMPATIBLE, profile.support)
    }

    @Test fun bydIsUnsupported() {
        val profile = LynkCompatibility.detect(
            "BYD", "BYD", "DiLink 5.0", "byd", "BYD-AUTO/IVI", "DiLink", "SA8155P", 33,
        )
        assertEquals(LynkModel.UNKNOWN, profile.model)
        assertEquals(LynkSupport.UNSUPPORTED, profile.support)
    }

    @Test fun unlistedLynkModelIsUnsupported() {
        val profile = LynkCompatibility.detect(
            "Lynk & Co", "Lynk", "Lynk & Co 08", "osn", "Lynk/OSN/08", "OSN 2.0",
            "SA8155P", 33,
        )
        assertEquals(LynkModel.UNKNOWN, profile.model)
        assertEquals(LynkSupport.UNSUPPORTED, profile.support)
    }
}
