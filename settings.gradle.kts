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

rootProject.name = "Aliucord Plugins"

rootDir.resolve("plugins")
    .listFiles { file -> file.isDirectory && file.resolve("build.gradle.kts").exists() }!!
    .forEach { include(":plugins:${it.name}") }