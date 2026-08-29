# AstroVeda ProGuard / R8 Rules

# General Rules
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,*Annotation*

# Data Models and Entities (Preserve reflection/serialization for Firestore, Room, Moshi)
-keep @androidx.room.Entity class *
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** {
    <fields>;
    public <init>();
}

# Firebase (Auth, Firestore, Cloud Messaging, Analytics)
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.internal.firebase_auth.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.database.PropertyName <fields>;
    @com.google.firebase.database.PropertyName <methods>;
    public <init>();
}
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.data.local.** { *; }
-keepclassmembers class * {
    @androidx.room.* *;
}
-dontwarn androidx.room.**

# WorkManager Workers
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.example.worker.** { *; }

# Google Play Billing
-keep class com.android.billingclient.api.** { *; }
-dontwarn com.android.billingclient.api.**

# Google Play In-App Review
-keep class com.google.android.play.review.** { *; }
-dontwarn com.google.android.play.review.**

# Google Identity / Credentials (Google Sign-In)
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.**

# PDF Document Generation ( Kundali Matching PDF reports )
-keep class android.graphics.pdf.** { *; }
-dontwarn android.graphics.pdf.**

# Google Mobile Ads (AdMob)
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}
-keep class com.google.android.gms.internal.ads.** { *; }
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.ads.**

# Retrofit
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# Moshi
-keep class com.squareup.moshi.** { *; }
-keep class * extends com.squareup.moshi.JsonAdapter
-keep class * { @com.squareup.moshi.Json *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-dontwarn com.squareup.moshi.**

# Kotlin Serialization / Coroutines
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-dontwarn kotlinx.coroutines.**

# --- Security & Privacy Hardening ---
# Strip Android debug and verbose logging in release builds (prevents sensitive PII leaks)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# Preserve SecurityUtils and Billing integrity
-keep class com.example.util.SecurityUtils { *; }
-keepclassmembers class com.example.util.SecurityUtils {
    public static <methods>;
}

# JVM & Third-Party Library Warnings Suppression
-dontwarn java.lang.management.**
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.google.android.gms.internal.**


# User Messaging Platform (ad consent). play-services-ads' rules cover
# com.google.android.gms.ads.** but UMP lives in its own package.
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

# Firebase AI Logic serialises its request/response models with
# kotlinx.serialization; keep the generated serializers.
-keepclassmembers class com.google.firebase.ai.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.google.firebase.ai.** {
    kotlinx.serialization.KSerializer serializer(...);
}
