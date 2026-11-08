package com.shilapi.xcertplay

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.shilapi.xcertplay.lynk.LynkCompatibility

/** On-device checks that turn a confusing "it does not work" into an actionable car report. */
internal data class HeadUnitCheck(
    val id: String,
    val label: String,
    val status: Status,
    val detail: String,
) {
    enum class Status { PASS, FAIL, UNKNOWN }
}

internal object HeadUnitValidation {
    fun report(context: Context): String = buildString {
        appendLine("--- Head-unit validation ---")
        capture(context).forEach { check ->
            appendLine("${check.status.name} ${check.id}: ${check.label} — ${check.detail}")
        }
    }

    fun capture(context: Context): List<HeadUnitCheck> = buildList {
        val profile = runCatching { LynkCompatibility.current() }.getOrNull()
        add(
            check(
                "lynk_model",
                "Lynk model / platform",
                if (profile != null && profile.support != com.shilapi.xcertplay.lynk.LynkSupport.UNSUPPORTED) {
                    HeadUnitCheck.Status.PASS
                } else {
                    HeadUnitCheck.Status.FAIL
                },
                profile?.let { "${it.model.displayName}; ${it.platform}; ${it.generation}; ${it.support}; ${it.evidence}" }
                    ?: "not detected",
            ),
        )
        add(permission(context, "bluetooth", "Bluetooth", Manifest.permission.BLUETOOTH_CONNECT, 31))
        add(permission(context, "location", "Precise location", Manifest.permission.ACCESS_FINE_LOCATION, 0))
        add(permission(context, "notifications", "Notifications", Manifest.permission.POST_NOTIFICATIONS, 33))
        add(
            check(
                "overlay",
                "Display over other apps",
                if (Settings.canDrawOverlays(context)) HeadUnitCheck.Status.PASS else HeadUnitCheck.Status.FAIL,
                "needed only for the dashboard map card",
            ),
        )
        add(
            check(
                "write_settings",
                "Modify system settings",
                if (Settings.System.canWrite(context)) HeadUnitCheck.Status.PASS else HeadUnitCheck.Status.FAIL,
                "needed for hotspot / brightness automations",
            ),
        )
        add(
            check(
                "usage_access",
                "Usage access",
                if (hasUsageAccess(context)) HeadUnitCheck.Status.PASS else HeadUnitCheck.Status.FAIL,
                "lets the app follow the foreground launcher",
            ),
        )
        add(displayCheck(context))
        add(clusterCheck(context))
        add(audioCheck(context))
        add(
            check(
                "wheel_keys",
                "Wheel-key service",
                if (WheelKeyService.connected()) HeadUnitCheck.Status.PASS else HeadUnitCheck.Status.UNKNOWN,
                if (WheelKeyService.connected()) "connected" else "not connected; enable wheel keys in Vehicle settings",
            ),
        )
        add(
            check(
                "carplay_assets",
                "CarPlay authentication assets",
                if (hasAuthAssets(context)) HeadUnitCheck.Status.PASS else HeadUnitCheck.Status.FAIL,
                "identity.pk8 + certificate.p7b in the installed APK",
            ),
        )
        add(
            check(
                "language",
                "Language",
                HeadUnitCheck.Status.PASS,
                AppLocale.preference(context),
            ),
        )
        add(
            check(
                "sdk",
                "Android / build",
                HeadUnitCheck.Status.PASS,
                "${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}; ${Build.MANUFACTURER} ${Build.MODEL}; ${Build.DISPLAY}",
            ),
        )
    }

    private fun permission(
        context: Context,
        id: String,
        label: String,
        permission: String,
        minSdk: Int,
    ): HeadUnitCheck {
        if (Build.VERSION.SDK_INT < minSdk) {
            return check(id, label, HeadUnitCheck.Status.UNKNOWN, "not applicable on this Android version")
        }
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        return check(id, label, if (granted) HeadUnitCheck.Status.PASS else HeadUnitCheck.Status.FAIL, permission)
    }

    private fun displayCheck(context: Context): HeadUnitCheck {
        val bounds = if (Build.VERSION.SDK_INT >= 30) {
            context.getSystemService(WindowManager::class.java).currentWindowMetrics.bounds
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            context.getSystemService(WindowManager::class.java).defaultDisplay.getRealMetrics(metrics)
            android.graphics.Rect(0, 0, metrics.widthPixels, metrics.heightPixels)
        }
        val density = context.resources.displayMetrics.densityDpi
        val ok = bounds.width() >= 1280 && bounds.height() >= 600
        return check(
            "display",
            "Display",
            if (ok) HeadUnitCheck.Status.PASS else HeadUnitCheck.Status.UNKNOWN,
            "${bounds.width()}×${bounds.height()} @ ${density} dpi",
        )
    }

    private fun clusterCheck(context: Context): HeadUnitCheck {
        val displays = context.getSystemService(DisplayManager::class.java).displays
        val cluster = displays.filter {
            it.displayId != Display.DEFAULT_DISPLAY && it.flags and Display.FLAG_PRESENTATION != 0
        }
        val detail = if (cluster.isEmpty()) {
            "no presentation display"
        } else {
            cluster.joinToString { "${it.name}: ${it.mode?.physicalWidth}×${it.mode?.physicalHeight}" }
        }
        return check(
            "cluster",
            "Optional cluster display",
            if (cluster.isNotEmpty()) HeadUnitCheck.Status.PASS else HeadUnitCheck.Status.UNKNOWN,
            detail,
        )
    }

    private fun audioCheck(context: Context): HeadUnitCheck {
        val devices = runCatching {
            context.getSystemService(AudioManager::class.java)
                .getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                .joinToString { it.productName?.toString() ?: it.type.toString() }
        }.getOrDefault("")
        return check(
            "audio_output",
            "Audio outputs",
            if (devices.isBlank()) HeadUnitCheck.Status.UNKNOWN else HeadUnitCheck.Status.PASS,
            devices.ifBlank { "no output reported" },
        )
    }

    private fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val mode = if (Build.VERSION.SDK_INT >= 29) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun hasAuthAssets(context: Context): Boolean = runCatching {
        context.assets.open("offline-mfi/identity.pk8").use { }
        context.assets.open("offline-mfi/certificate.p7b").use { }
        true
    }.getOrDefault(false)

    private fun check(id: String, label: String, status: HeadUnitCheck.Status, detail: String) =
        HeadUnitCheck(id, label, status, detail)
}
