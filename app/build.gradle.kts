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
    versionCode = 3
    versionName = "1.2"

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
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/astroveda-upload-key.jks"
      storeFile = file(keystorePath)
      val envProps = Properties().apply { val f = rootProject.file(".env"); if (f.exists()) f.inputStream().use { load(it) } }
      storePassword = System.getenv("STORE_PASSWORD") ?: envProps.getProperty("STORE_PASSWORD") ?: error("STORE_PASSWORD is required (.env or environment variable) for release build")
      keyAlias = System.getenv("KEY_ALIAS") ?: envProps.getProperty("KEY_ALIAS") ?: error("KEY_ALIAS is required (.env or environment variable) for release build")
      keyPassword = System.getenv("KEY_PASSWORD") ?: envProps.getProperty("KEY_PASSWORD") ?: error("KEY_PASSWORD is required (.env or environment variable) for release build")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
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

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
// Room writes each version's schema here so migrations can be written against a
// real diff and verified in tests, instead of guessed at.
ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation("androidx.fragment:fragment-ktx:1.8.6")
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.text.googlefonts)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
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
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.location)
  implementation(libs.play.services.ads)
  // Google's consent SDK. AdMob requires a consent mechanism for EEA/UK users and
  // there was none, which is an AdMob policy violation wherever the app is served there.
  implementation(libs.user.messaging.platform)
  implementation(libs.play.review.ktx)
  implementation(libs.billing.ktx)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
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
  "ksp"(libs.moshi.kotlin.codegen)
}
