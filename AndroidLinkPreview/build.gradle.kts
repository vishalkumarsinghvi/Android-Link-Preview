 plugins {
    alias(libs.plugins.android.library)
     id("maven-publish")
}

android {
    namespace = "com.jaincomapny.androidlinkpreview"
    compileSdk = 36

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    publishing {
        singleVariant("release")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.jsoup)
    implementation(libs.okhttp)
}

 afterEvaluate {
     publishing {
         publications {
             create<MavenPublication>("release") {
                 from(components["release"])

                 groupId = "com.github.vishalkumarsinghvi"
                 artifactId = "android-link-preview"
                 version = "1.1"
             }
         }
     }
 }