package com.shilapi.xcertplay

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.shilapi.xcertplay.host.R
import com.shilapi.xcertplay.telecom.PhoneAudioRoute

/** Starts the CarPlay host after boot when the user has enabled the startup option. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        // Starts the watcher for other apps' calls when that setting is on, even without opening DiPlay.
        PhoneAudioRoute.attach(context)
        val launchEnabled = AirPlayPersistence.loadAutoStartOnBoot(context)
        StartupDiagnosticSnapshot.received(context, launchEnabled)
        val open = openIntent(context)
        if (launchEnabled) {
            try {
                context.startActivity(open)
                StartupDiagnosticSnapshot.launchResult(context)
            } catch (error: RuntimeException) {
                StartupDiagnosticSnapshot.launchResult(context, error)
                Log.w(TAG, "Boot auto-start could not launch DiPlayActivity", error)
            }
        }
        // The OSN stock launcher uses a package whitelist and may never list a sideloaded app.
        // Keep a tappable entry point in the notification shade even when auto-start is off.
        postOpenNotification(context, open)
    }

    /** Shaped like the launcher icon's intent: an active CarPlay session is returned to, not covered. */
    private fun openIntent(context: Context) = Intent(context, DiPlayActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
        addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP,
        )
    }

    private fun postOpenNotification(context: Context, open: Intent) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (!manager.areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, context.applicationInfo.loadLabel(context.packageManager), NotificationManager.IMPORTANCE_LOW),
            )
        }
        val pending = PendingIntent.getActivity(
            context,
            2,
            open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context).setPriority(Notification.PRIORITY_LOW)
        }
        val notification = builder
            .setSmallIcon(R.drawable.ic_diplay_notification)
            .setContentTitle(context.applicationInfo.loadLabel(context.packageManager))
            .setContentText(context.getString(R.string.open))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()
        runCatching { manager.notify(OPEN_NOTIFICATION_ID, notification) }
            .onFailure { Log.w(TAG, "Could not post the open shortcut", it) }
    }

    private companion object {
        const val TAG = "xcertplay-boot"
        const val CHANNEL = "osnplay_open"
        const val OPEN_NOTIFICATION_ID = 2
    }
}
