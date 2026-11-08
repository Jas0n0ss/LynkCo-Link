plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Optional local-only input. CI and ordinary source builds contain no accessory identity.
// The directory is honoured ONLY for the explicit assembleStandalone* tasks. This keeps a stray
// environment variable from silently putting credentials into a public preview or release build.
val requestedAssembleTasks = gradle.startParameter.taskNames.filter { it.contains("assemble", ignoreCase = true) }
val standaloneTasks = requestedAssembleTasks.filter { it.contains("Standalone", ignoreCase = true) }
if (standaloneTasks.isNotEmpty() && standaloneTasks.size != requestedAssembleTasks.size) {
    throw GradleException(
        "Do not mix assembleStandalone* with other assemble tasks: credentials are added to the " +
            "shared main assets and would leak into the public variant. Run them in separate invocations.",
    )
}
val standaloneBuild = standaloneTasks.isNotEmpty()
val configuredAuthenticationAssets = providers.environmentVariable("LYNKCO_LINK_AUTH_ASSETS_DIR").orNull
val localAuthenticationAssets = configuredAuthenticationAssets
    ?.takeIf { standaloneBuild }
    ?.let { file(it).canonicalFile }
if (configuredAuthenticationAssets != null && !standaloneBuild) {
    logger.warn(
        "LYNKCO_LINK_AUTH_ASSETS_DIR is set but ignored: use an assembleStandalone* task to bundle credentials.",
    )
}

android {
    namespace = "com.shilapi.xcertplay"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
        targetSdk = 37
        // LynkCo-Link 0.1.2 = DiPlay 0.2.22 baseline + Lynk OSN launcher compatibility.
        versionCode = 300
        versionName = "0.3.0"
    }

    localAuthenticationAssets?.let { sourceSets.getByName("main").assets.srcDir(it) }

    // Distribution edition: the standard build keeps the LynkCo-Link identity, the lynk build is the
    // in-car A/B package. Build types stay reserved for debug/preview/release.
    flavorDimensions += "edition"
    productFlavors {
        create("standard") {
            dimension = "edition"
            applicationId = "com.lynkco.link"
        }
        create("lynk") {
            dimension = "edition"
            // OSN Launcher3 only lists packages on its whitelist. The delivered app therefore uses
            // the whitelisted head-unit music package name; there is no separate launcher entry APK.
            applicationId = "com.kugou.android.auto"
            versionNameSuffix = "-lynk"
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(
                providers.environmentVariable("ANDROID_KEYSTORE_PATH")
                    .getOrElse("missing-release-keystore.jks"),
            )
            storePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").getOrElse("")
            keyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").getOrElse("")
            keyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").getOrElse("")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            optimization {
                enable = false
            }
            signingConfig = signingConfigs.getByName("release")
        }
        // Debug-signed, installable package for preview releases and UI review. The ".preview"
        // suffix gives it a stable, separate identity: preview and release certificates differ, so
        // sharing the public applicationId would force an uninstall and lose settings later.
        create("preview") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".preview"
            versionNameSuffix = "-preview"
            isDebuggable = false
            matchingFallbacks += "release"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(project(":common"))
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.app.projected)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// No implicit import. Only the two explicitly selected local runtime assets are allowed.
val credentialAssets = files(android.sourceSets.flatMap { source ->
    source.assets.directories.map { directory ->
        fileTree(directory) {
            include("**/offline-mfi/**", "**/*.pk8", "**/*.p7b", "**/*.key",
                "**/*.pem", "**/*.p12", "**/*.pfx", "**/*.jks", "**/*.keystore")
        }
    }
})
val rejectBundledCredentials by tasks.registering {
    group = "verification"
    description = "Reject unexpected credential files in APK assets."
    val filesToCheck = credentialAssets
    val allowed = localAuthenticationAssets?.let { dir ->
        listOf("identity.pk8", "certificate.p7b").map { dir.resolve("offline-mfi/$it").canonicalFile }.toSet()
    } ?: emptySet()
    inputs.files(filesToCheck)
    doLast {
        check(allowed.all { it.isFile }) { "Explicit local authentication assets are incomplete" }
        val unexpected = filesToCheck.files.filter { it.canonicalFile !in allowed }
        check(unexpected.isEmpty()) { "Unexpected credential files in APK assets" }
    }
}
tasks.named("preBuild") { dependsOn(rejectBundledCredentials) }

// Car-test packages must be standalone. Keep ordinary source/CI builds identity-free.
val verifyStandaloneAuthentication by tasks.registering {
    group = "verification"
    description = "Require the explicit runtime authentication input for a standalone car-test APK."
    val directory = localAuthenticationAssets
    doLast {
        check(directory != null) {
            "Standalone car builds require LYNKCO_LINK_AUTH_ASSETS_DIR; assembleStandardDebug alone is source-only."
        }
        check(listOf("identity.pk8", "certificate.p7b").all {
            directory.resolve("offline-mfi/$it").let { file -> file.isFile && file.length() > 0 }
        }) { "Standalone CarPlay authentication files are missing or empty."
        }
    }
}
tasks.named("preBuild") { mustRunAfter(verifyStandaloneAuthentication) }
tasks.register("assembleStandaloneDebug") {
    group = "build"
    description = "Build a standalone car-test APK with explicitly provisioned authentication."
    dependsOn(verifyStandaloneAuthentication, "assembleStandardDebug")
}
tasks.register("assembleStandalonePreview") {
    group = "build"
    description = "Build a debug-signed standard preview APK with explicitly provisioned authentication."
    dependsOn(verifyStandaloneAuthentication, "assembleStandardPreview")
}
tasks.register("assembleStandaloneLynkPreview") {
    group = "build"
    description = "Build a debug-signed Lynk preview APK with explicitly provisioned authentication."
    dependsOn(verifyStandaloneAuthentication, "assembleLynkPreview")
}
tasks.register("assembleStandaloneLynkRelease") {
    group = "build"
    description = "Build a release-signed Lynk APK with explicitly provisioned authentication."
    dependsOn(verifyStandaloneAuthentication, "assembleLynkRelease")
}
tasks.register("assembleStandaloneStandardRelease") {
    group = "build"
    description = "Build a release-signed standard APK with explicitly provisioned authentication."
    dependsOn(verifyStandaloneAuthentication, "assembleStandardRelease")
}
