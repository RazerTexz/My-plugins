import com.aliucord.gradle.AliucordExtension

import com.android.build.gradle.LibraryExtension

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension

plugins {
    alias(libs.plugins.aliucord)
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
}

subprojects {
    if (name == "plugins") {
        return@subprojects
    }

    val libs = rootProject.libs

    pluginManager.apply(libs.plugins.aliucord.get().pluginId)
    pluginManager.apply(libs.plugins.android.library.get().pluginId)
    pluginManager.apply(libs.plugins.kotlin.android.get().pluginId)

    configure<AliucordExtension> {
        github("https://github.com/RazerTexz/My-plugins")
    }

    configure<LibraryExtension> {
        namespace = "com.github.razertexz"
        compileSdk = 36

        defaultConfig {
            minSdk = 21
        }

        buildFeatures {
            resValues = false
            shaders = false
        }

        sourceSets {
            named("main") {
                java {
                    setSrcDirs(setOf("src"))
                }

                res {
                    setSrcDirs(setOf("src/res"))
                }
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }

    configure<KotlinAndroidExtension> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    dependencies {
        val compileOnly by configurations

        compileOnly(libs.aliucord)
        compileOnly(libs.discord)
        compileOnly(libs.kotlin.stdlib)
    }
}