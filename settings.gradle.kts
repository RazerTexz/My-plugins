pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        maven("https://maven.aliucord.com/releases")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliucord.com/releases")
    }
}

rootDir.resolve("plugins").listFiles().forEach {
    if (it.isDirectory && it.resolve("build.gradle.kts").exists()) {
        include(":plugins:${it.name}")
    }
}

rootProject.name = "Aliucord Plugins"