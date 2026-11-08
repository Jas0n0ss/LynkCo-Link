package com.shilapi.xcertplay

import android.app.Application
import java.io.File

/** One-time bridge from the old preference file names to the LynkCo-Link names. */
class LynkCoLinkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        migrateLegacyPreferences()
    }

    private fun migrateLegacyPreferences() {
        val oldPrefix = "osn" + "play" // kept split so the old brand is not reintroduced in UI/code search
        runCatching {
            val dir = File(dataDir, "shared_prefs")
            dir.listFiles { file -> file.name.startsWith(oldPrefix) }?.forEach { old ->
                val newName = old.name.replaceFirst(oldPrefix, "lynkco_link")
                val target = File(dir, newName)
                if (!target.exists()) {
                    runCatching { old.copyTo(target, overwrite = false) }
                }
            }
        }
    }
}
