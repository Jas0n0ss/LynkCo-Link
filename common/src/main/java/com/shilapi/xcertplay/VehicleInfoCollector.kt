package com.shilapi.xcertplay

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.hardware.usb.UsbManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.shilapi.xcertplay.lynk.LynkCompatibility
import java.io.File
import java.util.concurrent.TimeUnit

/** Collects the device facts that are actually useful when a specific car misbehaves. */
internal object VehicleInfoCollector {
    private const val MAX_DUMPSYS = 160_000
    private const val MAX_UI_NODES = 600

    fun collect(context: Context, rootView: View?): Map<String, String> = linkedMapOf(
        "vehicle.txt" to vehicle(context),
        "build.txt" to build(context),
        "display.txt" to display(context),
        "audio.txt" to audio(context),
        "usb-input.txt" to usbAndInput(context),
        "bluetooth-wifi.txt" to bluetoothAndWifi(context),
        "permissions.txt" to permissions(context),
        "ui.txt" to viewTree(rootView),
        "getprop.txt" to redact(command("getprop") ?: "getprop unavailable"),
        "dumpsys-display.txt" to dumpsys("display"),
        "dumpsys-audio.txt" to dumpsys("audio"),
        "dumpsys-media-session.txt" to dumpsys("media_session"),
        "dumpsys-bluetooth.txt" to dumpsys("bluetooth_manager"),
        "dumpsys-wifi.txt" to dumpsys("wifi"),
        "dumpsys-input.txt" to dumpsys("input"),
        "dumpsys-window.txt" to dumpsys("window displays"),
        "dumpsys-package.txt" to dumpsys("package ${context.packageName}"),
    )

    private fun vehicle(context: Context): String = buildString {
        appendLine("--- Lynk profile ---")
        runCatching { LynkCompatibility.current() }.onSuccess {
            appendLine("model=${it.model.displayName}; platform=${it.platform}; generation=${it.generation}; support=${it.support}")
            appendLine("evidence=${it.evidence}")
        }.onFailure { appendLine("profile unavailable=${it.javaClass.simpleName}") }
        appendLine()
        appendLine(HeadUnitValidation.report(context))
    }

    private fun build(context: Context): String = buildString {
        val info = runCatching { context.packageManager.getPackageInfo(context.packageName, 0) }.getOrNull()
        appendLine("app=${info?.versionName} (${info?.versionCode})")
        appendLine("package=${context.packageName}")
        appendLine("manufacturer=${Build.MANUFACTURER}")
        appendLine("brand=${Build.BRAND}")
        appendLine("model=${Build.MODEL}")
        appendLine("device=${Build.DEVICE}")
        appendLine("product=${Build.PRODUCT}")
        appendLine("board=${Build.BOARD}")
        appendLine("hardware=${Build.HARDWARE}")
        appendLine("build=${Build.DISPLAY}")
        appendLine("fingerprint=${Build.FINGERPRINT}")
        appendLine("android=${Build.VERSION.RELEASE} API=${Build.VERSION.SDK_INT}")
        appendLine("abis=${Build.SUPPORTED_ABIS.joinToString()}")
        appendLine("locale=${java.util.Locale.getDefault().toLanguageTag()}")
        appendLine("uptimeMs=${android.os.SystemClock.elapsedRealtime()}")
    }

    private fun display(context: Context): String = buildString {
        val wm = context.getSystemService(WindowManager::class.java)
        if (Build.VERSION.SDK_INT >= 30) {
            val b = wm.currentWindowMetrics.bounds
            appendLine("window=${b.width()}x${b.height()}")
        }
        appendLine("density=${context.resources.displayMetrics.densityDpi}")
        val displays = context.getSystemService(DisplayManager::class.java).displays
        displays.forEach { d ->
            appendLine(
                "display id=${d.displayId} name=${d.name} flags=${d.flags} " +
                    "mode=${d.mode?.physicalWidth}x${d.mode?.physicalHeight}@${d.mode?.refreshRate} " +
                    "state=${d.state}",
            )
        }
    }

    private fun audio(context: Context): String = runCatching {
        val am = context.getSystemService(AudioManager::class.java)
        buildString {
            appendLine("outputs:")
            am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).forEach {
                appendLine("  type=${it.type} name=${it.productName} addr=${it.address}")
            }
            appendLine("inputs:")
            am.getDevices(AudioManager.GET_DEVICES_INPUTS).forEach {
                appendLine("  type=${it.type} name=${it.productName} addr=${it.address}")
            }
            appendLine("musicVolume=${am.getStreamVolume(AudioManager.STREAM_MUSIC)}/${am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)}")
        }
    }.getOrElse { "audio unavailable=${it.javaClass.simpleName}" }

    private fun usbAndInput(context: Context): String = buildString {
        runCatching {
            val usb = context.getSystemService(UsbManager::class.java)
            usb.deviceList.values.forEach {
                appendLine("usb vendor=${it.vendorId} product=${it.productId} class=${it.deviceClass} ${it.productName}")
            }
        }.onFailure { appendLine("usb unavailable=${it.javaClass.simpleName}") }
        runCatching {
            android.view.InputDevice.getDeviceIds().forEach { id ->
                val d = android.view.InputDevice.getDevice(id)
                appendLine("input id=$id name=${d?.name} sources=${d?.sources} keyboard=${d?.keyboardType}")
            }
        }.onFailure { appendLine("input unavailable=${it.javaClass.simpleName}") }
    }

    @Suppress("DEPRECATION")
    private fun bluetoothAndWifi(context: Context): String = buildString {
        runCatching {
            val adapter = context.getSystemService(BluetoothManager::class.java).adapter
            appendLine("bluetooth enabled=${adapter?.isEnabled} name=${adapter?.name} state=${adapter?.state}")
            adapter?.bondedDevices?.forEach { appendLine("bonded=${it.name} ${it.address}") }
        }.onFailure { appendLine("bluetooth unavailable=${it.javaClass.simpleName}") }
        runCatching {
            val wifi = context.applicationContext.getSystemService(WifiManager::class.java)
            appendLine("wifi enabled=${wifi.isWifiEnabled}")
            appendLine("wifi state=${wifi.wifiState}")
            wifi.connectionInfo?.let {
                appendLine("wifi ssid=${it.ssid} ip=${it.ipAddress} speed=${it.linkSpeed} freq=${it.frequency}")
            }
        }.onFailure { appendLine("wifi unavailable=${it.javaClass.simpleName}") }
    }

    private fun permissions(context: Context): String = buildString {
        val names = listOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
        )
        names.forEach { name ->
            val granted = ContextCompat.checkSelfPermission(context, name) == PackageManager.PERMISSION_GRANTED
            appendLine("$name=$granted")
        }
        appendLine("overlay=${android.provider.Settings.canDrawOverlays(context)}")
        appendLine("writeSettings=${android.provider.Settings.System.canWrite(context)}")
    }

    private fun viewTree(root: View?): String {
        if (root == null) return "view tree unavailable"
        val out = StringBuilder()
        var count = 0
        fun walk(view: View, depth: Int) {
            if (count++ > MAX_UI_NODES) return
            val id = runCatching { view.resources.getResourceEntryName(view.id) }.getOrDefault("")
            val text = if (view is EditText && view.inputType and android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD != 0) {
                "[password field]"
            } else {
                (view as? TextView)?.text?.toString()
            }
            val desc = view.contentDescription?.toString()
            out.append("  ".repeat(depth.coerceAtMost(12)))
                .append(view.javaClass.simpleName)
                .append(" id=").append(id)
                .append(" bounds=").append(view.left).append(',').append(view.top)
                .append(',').append(view.right).append(',').append(view.bottom)
                .append(" visible=").append(view.visibility)
                .append(" enabled=").append(view.isEnabled)
                .append(" focused=").append(view.isFocused)
            if (!text.isNullOrBlank()) out.append(" text=").append(DiagnosticRedactor.redact(text) ?: "[redacted]")
            if (!desc.isNullOrBlank()) out.append(" desc=").append(DiagnosticRedactor.redact(desc) ?: "[redacted]")
            out.append('\n')
            if (view is ViewGroup) for (i in 0 until view.childCount) walk(view.getChildAt(i), depth + 1)
        }
        walk(root, 0)
        return out.toString()
    }

    private fun dumpsys(service: String): String =
        redact(command("dumpsys", *service.split(" ").toTypedArray()) ?: "dumpsys $service unavailable")

    private fun command(vararg args: String): String? = runCatching {
        val process = ProcessBuilder(*args).redirectErrorStream(true).start()
        val text = process.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        process.waitFor(3, TimeUnit.SECONDS)
        text.take(MAX_DUMPSYS)
    }.getOrNull()

    private fun redact(text: String): String = text.lineSequence()
        .filterNot { it.startsWith("ro.serialno=") || it.startsWith("ro.boot.serialno=") }
        .mapNotNull(DiagnosticRedactor::redact)
        .joinToString("\n")
}
