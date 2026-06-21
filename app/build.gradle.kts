import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}
val admobAppId = localProperties.getProperty("ADMOB_APP_ID", "")
val admobAdUnitId = localProperties.getProperty("ADMOB_AD_UNIT_ID", "")
val keystorePath = localProperties.getProperty("KEYSTORE_PATH", "")
val keystorePassword = localProperties.getProperty("KEYSTORE_PASSWORD", "")
val keystoreKeyAlias = localProperties.getProperty("KEY_ALIAS", "")
val keystoreKeyPassword = localProperties.getProperty("KEY_PASSWORD", "")

android {
    namespace = "com.jaincomapny.android_link_preview"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = file(keystorePath)
            storePassword = keystorePassword
            keyAlias = keystoreKeyAlias
            keyPassword = keystoreKeyPassword
        }
    }

    defaultConfig {
        applicationId = "com.jaincomapny.android_link_preview"
        minSdk = 23
        targetSdk = 36
        versionCode = 5
        versionName = "1.3"

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
            signingConfig = signingConfigs.getByName("release")
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

    // Firebase (BOM manages all Firebase library versions)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}