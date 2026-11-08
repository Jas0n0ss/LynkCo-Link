package com.shilapi.xcertplay

import android.content.Context
import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class UsbAutoConfirmServiceTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun isEnabledReturnsFalseByDefault() {
        Settings.Secure.putString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            null,
        )
        assertFalse(UsbAutoConfirmService.isEnabled(context))
    }

    @Test
    fun isEnabledReturnsTrueWhenConfiguredInSecureSettings() {
        val serviceName = "${context.packageName}/${UsbAutoConfirmService::class.java.name}"
        Settings.Secure.putString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            "other.package/other.Service:$serviceName",
        )
        assertTrue(UsbAutoConfirmService.isEnabled(context))
    }
    @Test fun missingAccessibilitySettingsDoesNotCrash() {
        val unavailable = org.mockito.Mockito.mock(Context::class.java)
        org.mockito.Mockito.doThrow(android.content.ActivityNotFoundException())
            .`when`(unavailable).startActivity(org.mockito.ArgumentMatchers.any(android.content.Intent::class.java))
        assertFalse(UsbAutoConfirmService.openSettings(unavailable))
    }

    @Test fun onlySystemUsbActivitiesAreAccepted() {
        assertTrue(UsbAutoConfirmService.isSystemUsbWindow("com.android.systemui", "com.android.systemui.usb.UsbPermissionActivity"))
        assertFalse(UsbAutoConfirmService.isSystemUsbWindow("evil.app", "com.android.systemui.usb.UsbPermissionActivity"))
        assertFalse(UsbAutoConfirmService.isSystemUsbWindow("com.android.systemui", "android.app.AlertDialog"))
        assertFalse(UsbAutoConfirmService.isSystemUsbWindow("com.android.systemui", "com.android.systemui.media.MediaProjectionPermissionActivity"))
    }

    @Test fun theUsbDialogIsSeenUnderTheNameAlertActivityReports() {
        // AlertActivity reports UsbPermissionActivity as android.app.Dialog (#409).
        assertTrue(UsbAutoConfirmService.isSystemUsbWindow("com.android.systemui", "android.app.Dialog"))
        assertFalse(UsbAutoConfirmService.isSystemUsbWindow("evil.app", "android.app.Dialog"))
    }

    @Test fun otherSystemDialogsNamingLynkCoLinkAreLeftAlone() {
        assertFalse(UsbAutoConfirmService.isTargetPrompt("Start recording or casting with LynkCoLink?", "LynkCoLink"))
        assertFalse(UsbAutoConfirmService.isTargetPrompt(
            "LynkCoLink will have access to all of the information that is visible on your screen", "LynkCoLink",
        ))
        assertFalse(UsbAutoConfirmService.isTargetPrompt("要开始使用LynkCoLink录制或投放内容吗？", "LynkCoLink"))
    }

    @Test fun promptMustNameLynkCoLinkInEveryAndroidWording() {
        // Android 7.1 and 10+ wording, in English and Chinese (#409, #521).
        assertTrue(UsbAutoConfirmService.isTargetPrompt("Allow the app LynkCoLink to access the USB device?", "LynkCoLink"))
        assertTrue(UsbAutoConfirmService.isTargetPrompt("允许应用“LynkCoLink”访问该USB设备吗？", "LynkCoLink"))
        assertTrue(UsbAutoConfirmService.isTargetPrompt("Allow LynkCoLink to access iPhone?", "LynkCoLink"))
        assertTrue(UsbAutoConfirmService.isTargetPrompt("要允许LynkCoLink访问iPhone吗？", "LynkCoLink"))
        assertTrue(UsbAutoConfirmService.isTargetPrompt("Always open LynkCoLink when iPhone is connected", "LynkCoLink"))
        assertFalse(UsbAutoConfirmService.isTargetPrompt("Allow CarPlay access to iPhone?", "LynkCoLink"))
        assertFalse(UsbAutoConfirmService.isTargetPrompt("Allow FakeLynkCoLink to access iPhone?", "LynkCoLink"))
        assertFalse(UsbAutoConfirmService.isTargetPrompt("要允许LynkCoLinker访问iPhone吗？", "LynkCoLink"))
        assertFalse(UsbAutoConfirmService.isTargetPrompt("Allow USB access?", ""))
    }

        @Test fun systemUsbPromptClicksOnlyWhenTheNamedAppMatches() {
        val service = org.mockito.Mockito.spy(org.robolectric.Robolectric.buildService(UsbAutoConfirmService::class.java).create().get())
        val root = org.mockito.Mockito.mock(android.view.accessibility.AccessibilityNodeInfo::class.java)
        val button = org.mockito.Mockito.mock(android.view.accessibility.AccessibilityNodeInfo::class.java)
        org.mockito.Mockito.doReturn(root).`when`(service).rootInActiveWindow
        org.mockito.Mockito.`when`(root.windowId).thenReturn(42)
        org.mockito.Mockito.`when`(root.packageName).thenReturn("com.android.systemui")
        org.mockito.Mockito.`when`(root.childCount).thenReturn(1)
        org.mockito.Mockito.`when`(root.getChild(0)).thenReturn(button)
        org.mockito.Mockito.`when`(button.isEnabled).thenReturn(true)
        org.mockito.Mockito.`when`(button.isClickable).thenReturn(true)
        org.mockito.Mockito.`when`(button.viewIdResourceName).thenReturn("android:id/button1")
        org.mockito.Mockito.`when`(button.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)).thenReturn(true)
        val event = org.mockito.Mockito.mock(android.view.accessibility.AccessibilityEvent::class.java)
        org.mockito.Mockito.`when`(event.eventType).thenReturn(android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
        org.mockito.Mockito.`when`(event.packageName).thenReturn("com.android.systemui")
        org.mockito.Mockito.`when`(event.className).thenReturn("com.android.systemui.usb.UsbPermissionActivity")
        org.mockito.Mockito.`when`(event.windowId).thenReturn(42)
        try {
            org.mockito.Mockito.`when`(root.text).thenReturn("Allow OtherApp to access this USB device?")
            service.onAccessibilityEvent(event)
            org.mockito.Mockito.verify(button, org.mockito.Mockito.never()).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
            val label = service.applicationInfo.loadLabel(service.packageManager)
            org.mockito.Mockito.`when`(root.text).thenReturn("Allow $label to access this USB device?")
            service.onAccessibilityEvent(event)
            org.mockito.Mockito.verify(button).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
        } finally {
            event.recycle()
        }
    }
}
