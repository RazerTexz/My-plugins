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

for (folder in rootDir.resolve("plugins").listFiles { it.isDirectory }) {
    include(":plugins:${folder.name}")
}