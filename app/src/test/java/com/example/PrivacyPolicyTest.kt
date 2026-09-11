package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The privacy policy is kept twice: `docs/PRIVACY_POLICY.md` is the URL on the Play
 * listing and what a reviewer reads, and `assets/privacy_policy.html` is what
 * Settings opens. By September 2026 they had become two different documents. The
 * published one said an account was required, after the sign-in gate had been
 * removed and against the app's own Data safety form; the in-app one described
 * push-notification tokens for an app with no push messaging; neither mentioned
 * Firebase Analytics, which the app calls from dozens of places.
 *
 * `docs/render_privacy_policy.py` writes the HTML from the markdown. These tests
 * keep the two copies saying the same words, and keep both honest about which
 * Google services the build actually ships.
 */
class PrivacyPolicyTest {

    private val root: File = generateSequence(File(System.getProperty("user.dir")!!).absoluteFile) { it.parentFile }
        .first { File(it, "settings.gradle.kts").exists() }

    private val markdown = File(root, "docs/PRIVACY_POLICY.md").readText()
    private val html = File(root, "app/src/main/assets/privacy_policy.html").readText()

    // What a reader sees, on one line. Searching the raw markdown missed a phrase
    // the old policy did contain — "Firebase Crashlytics" was wrapped across two
    // lines — so every phrase check runs against this instead.
    private val policy = words(markdownText(markdown)).joinToString(" ")

    @Test
    fun `the in-app copy says exactly what the published copy says`() {
        val published = words(markdownText(markdown))
        val inApp = words(htmlText(html))
        val at = published.indices.firstOrNull { it >= inApp.size || published[it] != inApp[it] }
            ?: if (inApp.size > published.size) published.size else null
        if (at != null) {
            fun around(w: List<String>) =
                w.subList(maxOf(0, at - 6), minOf(w.size, at + 6)).joinToString(" ")
            throw AssertionError(
                "privacy_policy.html has drifted from docs/PRIVACY_POLICY.md at word $at.\n" +
                    "  markdown: ...${around(published)}...\n" +
                    "  html:     ...${around(inApp)}...\n" +
                    "Edit the markdown, then run: python3 docs/render_privacy_policy.py"
            )
        }
    }

    @Test
    fun `every Google service in the build is named in the policy, and no other`() {
        val shipped = Regex("""(?m)^\s*implementation\(libs\.([a-z0-9.]+)\)""")
            .findAll(File(root, "app/build.gradle.kts").readText())
            .map { it.groupValues[1] }
            .toSet()
        val problems = disclosures.mapNotNull { (library, phrase) ->
            val inBuild = library in shipped
            val inPolicy = policy.contains(phrase)
            when {
                inBuild && !inPolicy -> "$library ships in the app, but the policy never says \"$phrase\""
                !inBuild && inPolicy -> "the policy says \"$phrase\", but $library is not in the app"
                else -> null
            }
        }
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun `precise location is disclosed exactly when the manifest asks for it`() {
        val asks = File(root, "app/src/main/AndroidManifest.xml").readText()
            .contains("android.permission.ACCESS_FINE_LOCATION")
        assertEquals(
            "ACCESS_FINE_LOCATION in the manifest and \"precise location\" in the policy must agree",
            asks,
            policy.contains("precise location")
        )
    }

    @Test
    fun `the Tele-MANAS line survives in the policy`() {
        // Removed from the AI disclaimer on the owner's instruction. The policy was
        // deliberately not part of that instruction — see Ground rules in CLAUDE.md.
        assertTrue(policy.contains("Tele-MANAS (14416)"))
    }

    private val disclosures = mapOf(
        "firebase.auth" to "Firebase Authentication",
        "googleid" to "Google Sign-In",
        "firebase.firestore" to "Cloud Firestore",
        "firebase.ai" to "Firebase AI Logic",
        "firebase.analytics" to "Firebase Analytics",
        "firebase.crashlytics" to "Firebase Crashlytics",
        "firebase.appcheck.playintegrity" to "Firebase App Check",
        "play.services.ads" to "AdMob",
        "user.messaging.platform" to "User Messaging Platform",
        "billing.ktx" to "Google Play Billing",
        "play.review.ktx" to "In-App Review",
        // Neither is in the app. The first is here because an earlier policy
        // claimed it; both are the obvious next thing to add without remembering
        // that this file has to change too.
        "firebase.messaging" to "Cloud Messaging",
        "firebase.storage" to "Cloud Storage",
    )

    private fun words(text: String) = text.split(Regex("\\s+")).filter { it.isNotEmpty() }

    private fun markdownText(md: String) = md
        .replace(Regex("(?m)^\\s*---\\s*$"), " ")
        .replace(Regex("(?m)^\\s*#{1,6}\\s+"), " ")
        .replace(Regex("(?m)^\\s*-\\s+"), " ")
        .replace("**", "")
        .replace("*", "")

    private fun htmlText(page: String) = page
        .substringAfter("<body>").substringBefore("</body>")
        .replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), " ")
        .replace(Regex("</?(p|li|ul|ol|h[1-6]|div|br|hr)\\b[^>]*>"), " ")
        .replace(Regex("<[^>]+>"), "")
        .replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
        .replace("&#39;", "'").replace("&nbsp;", " ").replace("&amp;", "&")
}
