import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}
val admobAppId = localProperties.getProperty("ADMOB_APP_ID", "")
val admobAdUnitId = localProperties.getProperty("ADMOB_AD_UNIT_ID", "")

android {
    namespace = "com.jaincomapny.android_link_preview"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jaincomapny.android_link_preview"
        minSdk = 23
        targetSdk = 36
        versionCode = 4
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["admobAppId"] = admobAppId
        buildConfigField("String", "ADMOB_AD_UNIT_ID", "\"$admobAdUnitId\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isDebuggable = false
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":AndroidLinkPreview"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.accompanist.placeholder.material3)
    implementation(libs.compose)
    implementation(libs.play.services.ads)
    implementation(libs.okhttp)
    implementation(libs.androidx.material.icons.extended)

}