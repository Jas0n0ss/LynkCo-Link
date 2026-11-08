package com.shilapi.xcertplay

import android.content.Context
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Builds a shareable zip for field debugging: the normal diagnostic report, the app's own
 * logcat, the crash buffer and the retained session logs. Every line goes through
 * [DiagnosticRedactor]; no protocol payload, credential or identifier is exported.
 */
internal object DebugLogExporter {
    private const val MAX_LOG_CHARS = 400_000

    fun fileName(): String =
        "LynkCo-Link-debug-${SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.US).format(Date())}.zip"

    fun export(context: Context, report: String): ByteArray {
        val sessionLogs = LinkCoSessionLogs.read(context)
        return buildZip(
            report = report,
            logcat = capture("logcat", "-d", "-v", "threadtime", "-t", "5000"),
            crashLogcat = capture("logcat", "-b", "crash", "-d", "-v", "threadtime"),
            sessionLogs = sessionLogs,
            deviceInfo = deviceInfo(context),
        )
    }

    /** Separated from capture so the archive layout can be tested without a running logcat. */
    fun buildZip(
        report: String,
        logcat: String,
        crashLogcat: String,
        sessionLogs: Map<String, String>,
        deviceInfo: String,
    ): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.entry("README.txt", readme())
            zip.entry("device.txt", deviceInfo)
            zip.entry("diagnostic-report.txt", redact(report))
            zip.entry("logcat.txt", redact(logcat))
            zip.entry("logcat-crash.txt", redact(crashLogcat))
            sessionLogs.forEach { (name, content) ->
                zip.entry("session-logs/${name.replace(Regex("[^A-Za-z0-9._-]"), "_")}", redact(content))
            }
        }
        return output.toByteArray()
    }

    private fun ZipOutputStream.entry(name: String, text: String) {
        putNextEntry(ZipEntry(name))
        write(text.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun readme() = """
        LynkCo-Link debug log bundle
        =============================

        diagnostic-report.txt  What the app was doing at export time.
        device.txt             Android/head-unit/build identity.
        logcat.txt             Last 5,000 lines from this app's process.
        logcat-crash.txt       Android crash buffer.
        session-logs/          Retained LynkCo-Link session logs.

        Protocol payloads, credentials, SSIDs, IP/MAC addresses and long identifiers are redacted.
        Attach this zip to a bug report. No data is uploaded automatically.
    """.trimIndent()

    private fun deviceInfo(context: Context): String = buildString {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        appendLine("LynkCo-Link ${info.versionName} (${info.versionCode})")
        appendLine("Package: ${context.packageName}")
        appendLine("Android ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}")
        appendLine("Build: ${Build.DISPLAY}")
        appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Board: ${Build.BOARD}; hardware: ${Build.HARDWARE}")
        appendLine("Locale: ${Locale.getDefault().toLanguageTag()}")
    }

    private fun capture(vararg command: String): String = runCatching {
        val process = ProcessBuilder(*command).redirectErrorStream(true).start()
        val text = process.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        process.waitFor(5, TimeUnit.SECONDS)
        text.take(MAX_LOG_CHARS)
    }.getOrDefault("")

    private fun redact(text: String): String = text.lineSequence()
        .mapNotNull(DiagnosticRedactor::redact)
        .joinToString("\n")
}

/** Reads the same session logs the diagnostic report uses, without leaking the file path. */
private object LinkCoSessionLogs {
    fun read(context: Context): Map<String, String> = SessionLogFile.REPORT_NAMES.mapNotNull { name ->
        val file = File(context.filesDir, "logs/$name")
        if (!file.isFile) null else name to runCatching { file.readText().take(400_000) }.getOrDefault("")
    }.toMap()
}
