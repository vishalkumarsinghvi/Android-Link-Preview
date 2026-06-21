# ── Crash stack traces ────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Kotlin Essentials ─────────────────────────────────────────────────────────
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    <methods>;
}
-dontwarn kotlin.jvm.internal.ReflectionFactory

# ── Kotlin Coroutines ─────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext {
    private final android.os.Handler handler;
}
-dontwarn kotlinx.coroutines.**

# ── OkHttp 5.x ───────────────────────────────────────────────────────────────
-dontwarn okhttp3.internal.platform.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keep class okhttp3.internal.platform.** { *; }

# ── Jsoup ─────────────────────────────────────────────────────────────────────
-keep class org.jsoup.** { *; }
-keepclassmembers class org.jsoup.** { *; }

# ── Google Mobile Ads (AdMob) & Play Services ─────────────────────────────────
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ── Firebase ──────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ── Glide ─────────────────────────────────────────────────────────────────────
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

# ── AndroidX Lifecycle & ViewModel ───────────────────────────────────────────
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ── WorkManager + Room ────────────────────────────────────────────────────────
# WorkManager uses Room internally (WorkDatabase); R8 strips the generated
# _Impl class unless these rules are present. Firebase/AdMob pull in WorkManager
# as a transitive dep, so this is required even without direct WorkManager usage.
-keep class androidx.work.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Database class * { *; }

# ── Models (Keep data classes to avoid issues with reflection/parsers) ────────
# We keep all classes in these packages to ensure no business logic is stripped
-keep class com.jaincomapny.androidlinkpreview.** { *; }
-keep class com.jaincomapny.android_link_preview.** { *; }

# ── Suppress known-safe missing-class warnings ───────────────────────────────
-dontwarn com.google.re2j.Matcher
-dontwarn com.google.re2j.Pattern
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
