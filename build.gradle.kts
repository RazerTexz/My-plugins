import com.aliucord.gradle.AliucordExtension

import com.android.build.gradle.LibraryExtension

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension

plugins {
    id("com.aliucord.plugin") version "2.3.1"
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.4.0" apply false
}

subprojects {
    if (name == "plugins") return@subprojects

    pluginManager.apply("com.aliucord.plugin")
    pluginManager.apply("com.android.library")
    pluginManager.apply("org.jetbrains.kotlin.android")

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
        "compileOnly"("com.aliucord:Aliucord:2.9.5")
        "compileOnly"("com.discord:discord:126021")
        "compileOnly"("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
    }
}