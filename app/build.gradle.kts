import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
  alias(libs.plugins.google.firebase.crashlytics)
}

// Signing credentials are read here, but nothing is *required* here.
//
// These used to be `?: error(...)`, which runs at configuration time — so a
// missing STORE_PASSWORD failed every Gradle invocation, including
// `testDebugUnitTest`, on any machine without the real .env. That is CI (where
// .env is the example file by design, and must never hold a signing password)
// and it is anyone else who clones the repo. A release-only requirement was
// being enforced on every task. Missing credentials now leave the release
// signing config unconfigured, and the check moves to the release build itself,
// below, where it can actually be about a release.
val signingEnv = Properties().apply {
  val f = rootProject.file(".env")
  if (f.exists()) f.inputStream().use { load(it) }
}
fun signingValue(name: String): String? =
  System.getenv(name) ?: signingEnv.getProperty(name)?.takeIf { it.isNotBlank() }

// KEYSTORE_PATH used to be read from the environment only, and fell back to
// astroveda-upload-key.jks — the key Play stopped accepting. .env pointed at
// the live keystore and was ignored, so a release build signed itself with a
// dead key. The fallback is now the keystore Play actually accepts.
val releaseKeystore = file(signingValue("KEYSTORE_PATH") ?: "${rootDir}/upload-keystore.jks")
val releaseStorePassword = signingValue("STORE_PASSWORD")
val releaseKeyAlias = signingValue("KEY_ALIAS")
val releaseKeyPassword = signingValue("KEY_PASSWORD")
val canSignRelease = releaseKeystore.exists() &&
  releaseStorePassword != null && releaseKeyAlias != null && releaseKeyPassword != null

// The debug keystore is gitignored, so it is absent on CI and on a fresh
// clone. Falling back to the one AGP generates keeps those builds working;
// the checked-in keystore is only interesting locally, because its SHA-1 is
// the one registered in Firebase for Google Sign-In from debug builds.
val debugKeystore = file("${rootDir}/debug.keystore")

android {
  // The Kotlin sources still live under com.example; this is the applicationId
  // namespace, which is what R and BuildConfig are generated into.
  namespace = "app.revati.jyotish"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    // The app is called Revati; the package still says astroveda because the
    // first production release went out under it, and an applicationId is
    // permanent once published. Only the Play URL and Settings > App info ever
    // show it.
    applicationId = "com.aistudio.astroveda.kpvqzm"
    minSdk = 24
    targetSdk = 36
    versionCode = 9
    versionName = "1.4"

    val envProperties = Properties()
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
      envFile.inputStream().use { envProperties.load(it) }
    }
    manifestPlaceholders["ADMOB_APP_ID_ANDROID"] =
      envProperties.getProperty("ADMOB_APP_ID_ANDROID") ?: "ca-app-pub-3940256099942544~3347511713"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }


  signingConfigs {
    create("release") {
      if (canSignRelease) {
        storeFile = releaseKeystore
        storePassword = releaseStorePassword
        keyAlias = releaseKeyAlias
        keyPassword = releaseKeyPassword
      }
    }
    if (debugKeystore.exists()) {
      create("debugConfig") {
        storeFile = debugKeystore
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      // Unsigned rather than half-signed when credentials are absent; the check
      // below turns that into a clear failure if a release is actually asked for.
      if (canSignRelease) signingConfig = signingConfigs.getByName("release")
      // Play warns on upload: "This App Bundle contains native code, and you've
      // not uploaded debug symbols."
      //
      // That warning cannot be cleared from this project, and it is worth
      // writing down why so nobody burns an afternoon on it again. This app has
      // no native code of its own. The eight .so files arrive transitively from
      // androidx.graphics.path and androidx.datastore, and both are shipped
      // already stripped — checked on the versionCode 6 bundle:
      //
      //   libandroidx.graphics.path.so    ELF 64-bit … stripped, 0 symtab entries
      //   libdatastore_shared_counter.so  ELF 64-bit … stripped, 0 symtab entries
      //
      // debugSymbolLevel extracts symbols, it cannot invent them, so there is
      // nothing for it to package. The setting stays because it is correct in
      // principle: if a future dependency ever ships unstripped libraries, their
      // symbols will be picked up without anyone having to think about it. The
      // warning itself is advisory and does not block a release.
      ndk { debugSymbolLevel = "SYMBOL_TABLE" }
    }
    debug {
      if (debugKeystore.exists()) signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }

  // MigrationTestHelper reads the exported schema JSON out of the app's assets,
  // and the unit-test APK is built from the merged *debug* assets — a "test"
  // sourceSet entry never reaches it, which is why the migration tests failed
  // with FileNotFoundException. Debug-only, so the release APK does not ship the
  // schema files.
  sourceSets {
    getByName("debug") { assets.srcDirs(files("$projectDir/schemas")) }
    getByName("androidTest") { assets.srcDirs(files("$projectDir/schemas")) }
  }
  lint {
    abortOnError = true
    warningsAsErrors = false
    disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion", "IconDipSize", "IconLocation", "UnusedResources")
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Room writes each version's schema here so migrations can be written against a
// real diff and verified in tests, instead of guessed at.
ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
}

// Retrofit, OkHttp + logging-interceptor, Moshi (+ its KSP processor) and Coil
// were removed: all four were left over from when GeminiAstroService called the
// Gemini REST API directly, and had zero imports anywhere in the source. R8 was
// stripping the code, but the Moshi processor still ran on every build and each
// one was CVE surface carried for nothing.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation("androidx.fragment:fragment-ktx:1.8.6")
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.text.googlefonts)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.firebase.ai)
  implementation(libs.firebase.crashlytics)
  implementation(libs.firebase.analytics)
  implementation(libs.firebase.firestore)

  implementation(libs.firebase.auth)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services)
  implementation(libs.googleid)
  // App Check attests that requests really come from this app, signed with this
  // keystore, on a genuine device. It is what makes Firebase AI Logic safe to call
  // without shipping an API key. recaptcha is the WEB provider and does nothing on
  // Android; Play Integrity is the Android one.
  implementation(libs.firebase.appcheck.playintegrity)
  debugImplementation(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.play.services.location)
  implementation(libs.play.services.ads)
  // Google's consent SDK. AdMob requires a consent mechanism for EEA/UK users and
  // there was none, which is an AdMob policy violation wherever the app is served there.
  implementation(libs.user.messaging.platform)
  implementation(libs.play.review.ktx)
  implementation(libs.billing.ktx)
  implementation(libs.androidx.work.runtime.ktx)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.androidx.room.testing)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
}

// A release asked for without signing credentials would otherwise produce an
// unsigned bundle, which Play rejects after the upload rather than before it.
// The configuration-time `error(...)` that used to guard this broke every other
// task instead; this fires only when a release is actually in the task graph.
gradle.taskGraph.whenReady {
  val wantsRelease = allTasks.any { t ->
    t.name.contains("Release") &&
      (t.name.startsWith("assemble") || t.name.startsWith("bundle") || t.name.startsWith("publish"))
  }
  if (wantsRelease && !canSignRelease) {
    throw GradleException(
      "This is a release build and it cannot be signed.\n" +
        "  keystore: ${releaseKeystore.path} (exists: ${releaseKeystore.exists()})\n" +
        "  STORE_PASSWORD set: ${releaseStorePassword != null}\n" +
        "  KEY_ALIAS set: ${releaseKeyAlias != null}\n" +
        "  KEY_PASSWORD set: ${releaseKeyPassword != null}\n" +
        "Set these in .env or the environment. Debug builds and unit tests do " +
        "not need them and will run without."
    )
  }
}
