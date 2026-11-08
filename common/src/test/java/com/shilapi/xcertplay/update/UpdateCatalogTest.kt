package com.shilapi.xcertplay.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class UpdateCatalogTest {
    private val releaseJson = """
        [
          {
            "tag_name": "v0.2.15",
            "draft": true,
            "assets": [
              {"name": "LynkCoLink-0.2.15.apk", "browser_download_url": "https://github.com/shihabal3amri/LynkCoLink/releases/download/v0.2.15/LynkCoLink-0.2.15.apk"},
              {"name": "SHA256SUMS.txt", "browser_download_url": "https://github.com/shihabal3amri/LynkCoLink/releases/download/v0.2.15/SHA256SUMS.txt"}
            ]
          },
          {
            "tag_name": "v0.2.14",
            "draft": false,
            "prerelease": true,
            "assets": [
              {"name": "LynkCoLink-0.2.14.apk", "browser_download_url": "https://github.com/shihabal3amri/LynkCoLink/releases/download/v0.2.14/LynkCoLink-0.2.14.apk"},
              {"name": "SHA256SUMS.txt", "browser_download_url": "https://github.com/shihabal3amri/LynkCoLink/releases/download/v0.2.14/SHA256SUMS.txt"}
            ]
          }
        ]
    """.trimIndent()

    @Test
    fun skipsDraftsAndReturnsTheFirstPublishedRelease() {
        val release = UpdateCatalog.parse(releaseJson)
        assertEquals("v0.2.14", release?.tagName)
        assertEquals("LynkCoLink-0.2.14.apk", release?.apkName)
        assertEquals(
            "https://github.com/shihabal3amri/LynkCoLink/releases/download/v0.2.14/LynkCoLink-0.2.14.apk",
            release?.apkUrl,
        )
        assertEquals(
            "https://github.com/shihabal3amri/LynkCoLink/releases/download/v0.2.14/SHA256SUMS.txt",
            release?.checksumsUrl,
        )
    }

    @Test
    fun prefersTheStandardPackageWhenAReleaseCarriesBothEditions() {
        val json = """
            [
              {"tag_name": "v0.1.1-preview", "draft": false, "assets": [
                {"name": "LynkCoLink-0.1.1-preview-lynk.apk", "browser_download_url": "https://github.com/Jas0n0ss/OSN-Play/releases/download/v0.1.1-preview/LynkCoLink-0.1.1-preview-lynk.apk"},
                {"name": "LynkCoLink-0.1.1-preview-standard.apk", "browser_download_url": "https://github.com/Jas0n0ss/OSN-Play/releases/download/v0.1.1-preview/LynkCoLink-0.1.1-preview-standard.apk"},
                {"name": "SHA256SUMS.txt", "browser_download_url": "https://github.com/Jas0n0ss/OSN-Play/releases/download/v0.1.1-preview/SHA256SUMS.txt"}
              ]}
            ]
        """.trimIndent()
        val release = UpdateCatalog.parse(json)
        assertEquals("LynkCoLink-0.1.1-preview-standard.apk", release?.apkName)
    }

    @Test
    fun returnsNullWithoutAnApkAsset() {
        val json = """
            [
              {"tag_name": "v0.2.14", "draft": false, "assets": [
                {"name": "source.zip", "browser_download_url": "https://example.com/source.zip"}
              ]}
            ]
        """.trimIndent()
        assertNull(UpdateCatalog.parse(json))
    }

    @Test
    fun returnsNullWithoutChecksums() {
        val json = """
            [
              {"tag_name": "v0.2.14", "draft": false, "assets": [
                {"name": "LynkCoLink-0.2.14.apk", "browser_download_url": "https://example.com/LynkCoLink-0.2.14.apk"}
              ]}
            ]
        """.trimIndent()
        assertNull(UpdateCatalog.parse(json))
    }

    @Test
    fun returnsNullForAnEmptyReleaseList() {
        assertNull(UpdateCatalog.parse("[]"))
    }
    private fun releaseWith(apkName: String = "LynkCoLink-0.2.22.apk", apkUrl: String, checksumsUrl: String = GITHUB + "SHA256SUMS.txt") = """
        [{"tag_name": "v0.2.22", "draft": false, "assets": [
          {"name": ${org.json.JSONObject.quote(apkName)}, "browser_download_url": ${org.json.JSONObject.quote(apkUrl)}},
          {"name": "SHA256SUMS.txt", "browser_download_url": ${org.json.JSONObject.quote(checksumsUrl)}}
        ]}]
    """.trimIndent()

    @Test
    fun acceptsTheAssetsOfARealRelease() {
        val release = UpdateCatalog.parse(releaseWith(apkUrl = GITHUB + "LynkCoLink-0.2.22.apk"))
        assertEquals("LynkCoLink-0.2.22.apk", release?.apkName)
    }

    @Test
    fun refusesAnApkNameThatLeavesTheDownloadFolder() {
        for (name in listOf("../../evil.apk", "..\\evil.apk", "/data/evil.apk", "sub/evil.apk", ".apk", "evil.apk\u0000.txt.apk")) {
            assertNull(name, UpdateCatalog.parse(releaseWith(apkName = name, apkUrl = GITHUB + "x.apk")))
        }
    }

    @Test
    fun refusesDownloadUrlsThatAreNotHttpsOnGithub() {
        for (url in listOf(
            "http://github.com/shihabal3amri/LynkCoLink/releases/download/v0.2.22/LynkCoLink-0.2.22.apk",
            "https://evil.example/LynkCoLink-0.2.22.apk",
            "https://github.com.evil.example/LynkCoLink-0.2.22.apk",
            "https://github.com@evil.example/LynkCoLink-0.2.22.apk",
            "https://evil.example/github.com/LynkCoLink-0.2.22.apk",
            "file:///sdcard/LynkCoLink-0.2.22.apk",
            "ftp://github.com/LynkCoLink-0.2.22.apk",
        )) {
            assertNull(url, UpdateCatalog.parse(releaseWith(apkUrl = url)))
            assertNull(url, UpdateCatalog.parse(releaseWith(apkUrl = GITHUB + "x.apk", checksumsUrl = url)))
        }
    }

    private companion object {
        const val GITHUB = "https://github.com/shihabal3amri/LynkCoLink/releases/download/v0.2.22/"
    }
}
