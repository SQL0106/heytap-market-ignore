plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.sql916.marketignore"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.sql916.marketignore"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Provided by the framework inside the target process.
    compileOnly(libs.libxposed.api)
}
