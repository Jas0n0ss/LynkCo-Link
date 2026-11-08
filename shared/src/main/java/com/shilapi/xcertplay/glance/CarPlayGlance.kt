package com.shilapi.xcertplay.glance

import com.shilapi.xcertplay.iap2.message.Iap2LaneGuidance
import com.shilapi.xcertplay.iap2.message.LaneGuidanceLane
import com.shilapi.xcertplay.iap2.wire.Iap2Frame

/**
 * What CarPlay is doing, at a glance, for widgets and other screens outside CarPlay: the next
 * maneuver from iAP2 route guidance (0x5201/0x5202) and the song from NowPlayingUpdate (0x5001).
 * Fed by every CarPlay session, whatever the head unit output settings.
 */
object CarPlayGlance {
    /** One state of the glance; [maneuverType] is Apple's RouteGuidanceManeuverType, null without a route. */
    data class Snapshot(
        val connected: Boolean = false,
        val maneuverType: Int? = null,
        val drivingSide: Int = 0,
        val distanceMeters: Int = 0,
        val road: String = "",
        val arrivalEpochSeconds: Long? = null,
        val remainingSeconds: Long? = null,
        val remainingMeters: Long? = null,
        val song: String? = null,
        val playing: Boolean = false,
    )

    private const val LANE_GUIDANCE_UPDATE = 0x5204
    private const val TAG = "LynkCoLink-Lane"

    private var connected = false
    private var last = Snapshot()

    /** Called with each new snapshot, on the thread that changed it. */
    @Volatile var listener: ((Snapshot) -> Unit)? = null

    /** Lane guidance from 0x5204, on the iAP2 thread; empty when the lane strip goes away. */
    @Volatile var laneListener: ((List<LaneGuidanceLane>) -> Unit)? = null

    /** The last strip written to the log: Apple Maps repeats it while the overlap lasts. */
    private var loggedLanes: List<LaneGuidanceLane>? = null

    /** Refresh time-dependent guidance even when no new metadata frame has arrived. */
    fun snapshot(): Snapshot {
        val (changed, current) = synchronized(this) { publishLocked() to last }
        changed?.let { listener?.invoke(it) }
        return current
    }

    fun onFrame(frame: Iap2Frame) {
        if (frame.messageId == LANE_GUIDANCE_UPDATE) {
            // Lane guidance is a side channel: log it, hand it to whoever draws lanes, and leave
            // the route/now-playing state alone.
            val lanes = runCatching { Iap2LaneGuidance.parse(frame.payload) }.getOrDefault(emptyList())
            if (lanes != loggedLanes) {
                loggedLanes = lanes
                android.util.Log.i(TAG, "0x5204 " + describeLanes(lanes, frame.payload))
            }
            laneListener?.invoke(lanes)
            return
        }
        // This Lynk build has no head unit HUD/cluster consumer; lane guidance above is still logged.
        return
    }

    fun setConnected(next: Boolean) {
        val changed = synchronized(this) {
            if (connected == next) return
            connected = next
            publishLocked()
        }
        changed?.let { listener?.invoke(it) }
    }

    /**
     * One line per lane strip for the diagnostic report: the parsed fields, and the raw payload once,
     * so a report from a car we cannot test still shows what the phone actually sent.
     */
    private fun describeLanes(lanes: List<LaneGuidanceLane>, payload: ByteArray): String {
        if (lanes.isEmpty()) return "lane strip cleared"
        val strip = lanes.joinToString("; ") {
            "i=${it.index} st=${it.status} a=${it.angle} h=${it.highlightAngle}"
        }
        return "lanes=${lanes.size} [$strip] raw=" + payload.joinToString("") {
            "%02x".format(it.toInt() and 0xff)
        }
    }

    private fun publishLocked(): Snapshot? {
        val next = Snapshot(connected = connected)
        if (next == last) return null
        last = next
        return next
    }
}
