package com.kugou.android.auto;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Launcher entry for OSN Launcher3 whitelists. It borrows a whitelisted package name and
 * forwards to OSNPlay; it contains no CarPlay code or authentication material.
 */
public class MainActivity extends Activity {
    private static final String[][] TARGETS = {
        {"com.osnplay.lynk", "com.shilapi.xcertplay.OSNPlayActivity"},
        {"com.osnplay", "com.shilapi.xcertplay.OSNPlayActivity"},
        {"com.osnplay.lynk.preview", "com.shilapi.xcertplay.OSNPlayActivity"},
        {"com.osnplay.preview", "com.shilapi.xcertplay.OSNPlayActivity"},
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        PackageManager pm = getPackageManager();
        for (String[] target : TARGETS) {
            Intent intent = new Intent(Intent.ACTION_MAIN)
                .setComponent(new ComponentName(target[0], target[1]))
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (pm.resolveActivity(intent, 0) != null) {
                startActivity(intent);
                finish();
                return;
            }
        }
        Toast.makeText(this, "请先安装 OSNPlay", Toast.LENGTH_LONG).show();
        finish();
    }
}
