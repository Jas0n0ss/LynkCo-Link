package com.shilapi.xcertplay.lynk

import android.os.Build

/** Lynk & Co models this fork is allowed to target. */
enum class LynkModel(val number: String) {
    LYNK_01("01"),
    LYNK_02("02"),
    LYNK_03("03"),
    LYNK_05("05"),
    LYNK_06("06"),
    UNKNOWN("");

    val displayName: String get() = if (this == UNKNOWN) "Unknown" else "Lynk & Co $number"
}

enum class LynkGeneration { EARLY, LATE, OSN_2, UNKNOWN }

enum class LynkPlatform { SNAPDRAGON_8155, SNAPDRAGON_820A, OTHER, UNKNOWN }

enum class LynkSupport { PRIMARY, COMPATIBLE, UNSUPPORTED }

data class LynkProfile(
    val model: LynkModel,
    val generation: LynkGeneration,
    val platform: LynkPlatform,
    val support: LynkSupport,
    val evidence: String,
)

/**
 * Best-effort detection for the Lynk OSN family. The model number is only trusted when the
 * manufacturer/fingerprint identifies Lynk & Co; model codes vary between early and late units.
 */
object LynkCompatibility {
    private val MODEL_NUMBER = Regex("""(?i)(?:lynk\s*(?:&|and)?\s*co\s*)?\b(0?[1-6])\b""")
    private val MODEL_CODE = listOf(
        "CX11" to LynkModel.LYNK_01,
        "CC11" to LynkModel.LYNK_02,
        "CS11" to LynkModel.LYNK_03,
        "DCY11" to LynkModel.LYNK_05,
    )

    fun detect(
        manufacturer: String?,
        brand: String?,
        model: String?,
        device: String?,
        fingerprint: String?,
        display: String?,
        soc: String?,
        sdk: Int,
    ): LynkProfile {
        val text = listOfNotNull(manufacturer, brand, model, device, fingerprint, display, soc)
            .joinToString(" ").lowercase()
        val identityText = listOfNotNull(manufacturer, brand, model).joinToString(" ").lowercase()
        val isLynk = text.contains("lynk") || model?.startsWith("CS11_", ignoreCase = true) == true
        val detectedModel = if (isLynk) detectModel(model, identityText) else LynkModel.UNKNOWN
        val platform = when {
            text.contains("8155") || text.contains("sa8155") || text.contains("msmnile") -> LynkPlatform.SNAPDRAGON_8155
            text.contains("820a") || text.contains("msm8998") -> LynkPlatform.SNAPDRAGON_820A
            else -> if (isLynk) LynkPlatform.OTHER else LynkPlatform.UNKNOWN
        }
        val generation = when {
            Regex("""osn\s*2(?:\.0)?""", RegexOption.IGNORE_CASE).containsMatchIn(text) -> LynkGeneration.OSN_2
            sdk <= 28 -> LynkGeneration.EARLY
            sdk >= 29 -> LynkGeneration.LATE
            else -> LynkGeneration.UNKNOWN
        }
        val support = when {
            detectedModel == LynkModel.UNKNOWN -> LynkSupport.UNSUPPORTED
            platform == LynkPlatform.SNAPDRAGON_8155 && generation == LynkGeneration.OSN_2 -> LynkSupport.PRIMARY
            else -> LynkSupport.COMPATIBLE
        }
        return LynkProfile(
            model = detectedModel,
            generation = generation,
            platform = platform,
            support = support,
            evidence = listOfNotNull(model, display, soc).joinToString(" / ").take(120),
        )
    }

    fun current(): LynkProfile = detect(
        manufacturer = Build.MANUFACTURER,
        brand = Build.BRAND,
        model = Build.MODEL,
        device = Build.DEVICE,
        fingerprint = Build.FINGERPRINT,
        display = Build.DISPLAY,
        soc = if (Build.VERSION.SDK_INT >= 31) Build.SOC_MODEL else null,
        sdk = Build.VERSION.SDK_INT,
    )

    private fun detectModel(model: String?, text: String): LynkModel {
        val code = listOfNotNull(model, text).joinToString(" ")
        for ((prefix, lynk) in MODEL_CODE) {
            if (code.contains(prefix, ignoreCase = true)) return lynk
        }
        val match = MODEL_NUMBER.find(code) ?: return LynkModel.UNKNOWN
        return when (match.groupValues[1].trimStart('0')) {
            "1" -> LynkModel.LYNK_01
            "2" -> LynkModel.LYNK_02
            "3" -> LynkModel.LYNK_03
            "5" -> LynkModel.LYNK_05
            "6" -> LynkModel.LYNK_06
            else -> LynkModel.UNKNOWN
        }
    }
}
