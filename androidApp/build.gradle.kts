plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    // AGP 9 compiles Kotlin itself, so no kotlin-android plugin here.
}

android {
    namespace = "com.arin.pocketcompanion.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.arin.pocketcompanion"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        // 10.0.2.2 = your computer, as seen from the Android emulator.
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.compose.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
}
