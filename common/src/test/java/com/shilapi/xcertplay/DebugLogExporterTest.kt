package com.shilapi.xcertplay

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugLogExporterTest {
    @Test fun archiveContainsReportLogcatCrashAndSessionLogs() {
        val zip = DebugLogExporter.buildZip(
            report = "LynkCo-Link 0.3.3\nlast problem: none",
            logcat = "I/LynkCoLink: connected\nE/AndroidRuntime: crash",
            crashLogcat = "FATAL EXCEPTION: main",
            sessionLogs = mapOf("lynkco_link.log" to "session line"),
            deviceInfo = "Android 9 / API 28",
            vehicleFiles = mapOf("build.txt" to "model=test", "display.txt" to "1920x1080"),
        )
        val entries = unzip(zip)
        assertTrue(entries.containsKey("diagnostic-report.txt"))
        assertTrue(entries.containsKey("logcat.txt"))
        assertTrue(entries.containsKey("logcat-crash.txt"))
        assertTrue(entries.containsKey("device.txt"))
        assertTrue(entries.containsKey("session-logs/lynkco_link.log"))
        assertTrue(entries.containsKey("vehicle/build.txt"))
        assertTrue(entries.getValue("vehicle/display.txt").contains("1920x1080"))
        assertTrue(entries.getValue("logcat.txt").contains("connected"))
        assertTrue(entries.getValue("logcat-crash.txt").contains("FATAL"))
        assertTrue(entries.getValue("session-logs/lynkco_link.log").contains("session line"))
    }

    @Test fun secretsAndAddressesAreRedacted() {
        val zip = DebugLogExporter.buildZip(
            report = "password=hunter2\nip=192.168.1.20\nmac=aa:bb:cc:dd:ee:ff\nnormal line",
            logcat = "ssid=HomeWiFi\nnormal log",
            crashLogcat = "",
            sessionLogs = emptyMap(),
            deviceInfo = "device",
        )
        val entries = unzip(zip)
        val all = entries.values.joinToString("\n")
        assertFalse(all.contains("hunter2"))
        assertFalse(all.contains("HomeWiFi"))
        assertFalse(all.contains("192.168.1.20"))
        assertFalse(all.contains("aa:bb:cc:dd:ee:ff"))
        assertTrue(all.contains("normal line"))
        assertTrue(all.contains("normal log"))
    }

    @Test fun fileNameIsAZipWithBrand() {
        val name = DebugLogExporter.fileName()
        assertTrue(name.startsWith("LynkCo-Link-vehicle-report-"))
        assertTrue(name.endsWith(".zip"))
    }

    private fun unzip(bytes: ByteArray): Map<String, String> {
        val result = linkedMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                result[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        assertEquals(result.size, result.keys.distinct().size)
        return result
    }
}
