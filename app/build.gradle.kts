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
        versionName = "1.0.0"
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("stable") {
            storeFile = rootProject.file("keystore/marketignore.p12")
            storePassword = "marketignore"
            keyAlias = "marketignore"
            keyPassword = "marketignore"
            storeType = "PKCS12"
        }
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "VERBOSE_LOG", "true")
            signingConfig = signingConfigs.getByName("stable")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "VERBOSE_LOG", "false")
            // Side-load only: signed with the committed throwaway key so updates install in place.
            signingConfig = signingConfigs.getByName("stable")
        }
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
