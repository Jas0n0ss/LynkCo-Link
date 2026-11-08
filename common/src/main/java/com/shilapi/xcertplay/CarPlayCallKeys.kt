package com.shilapi.xcertplay

import android.content.Context
import com.shilapi.xcertplay.orchestration.CarPlayController

/** Lynk builds use the generic steering-wheel keys; BYD's call-key broadcast is not installed. */
internal object CarPlayCallKeys {
    fun install(context: Context) = Unit
    fun onKey(context: Context, keyCode: Int, down: Boolean, controller: CarPlayController? = null): Boolean = false
}
