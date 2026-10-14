plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "org.vectorcraft.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "org.vectorcraft.android"
        minSdk = 26
        targetSdk = 28 // Prototype only: update after testing Android storage/window handling
        versionCode = 1
        versionName = "0.1-prototype"
        ndk { abiFilters += listOf("arm64-v8a") }
    }
    sourceSets.getByName("main").jniLibs.srcDirs("src/main/jniLibs")
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
