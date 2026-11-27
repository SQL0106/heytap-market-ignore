pluginManagement {
    repositories {
        google()
        // Official Google mirror of Maven Central; avoids 429 from repo.maven.apache.org on CI.
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        mavenCentral()
    }
}

rootProject.name = "HeyTapMarketPermanentIgnore"

include(":app")
