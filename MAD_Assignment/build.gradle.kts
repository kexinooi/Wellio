plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.groupassignment"
    compileSdk = 36

    defaultConfig {
        applicationId = "my.edu.utar.assignment_2_v2"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase & AI Logic (Stable April 2026)
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.firebase:firebase-ai")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.firebase:firebase-appcheck-debug")

    // Guava for Java Futures (Fixes the ListenableFuture errors)
    implementation("com.google.guava:guava:33.5.0-android")

    constraints {
        implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    }
}