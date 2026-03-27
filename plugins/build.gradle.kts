import com.aliucord.gradle.AliucordExtension
import com.android.build.gradle.LibraryExtension

import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val projectNamespace = "com.github.razertexz"
val repositoryUrl = "https://github.com/RazerTexz/My-plugins"

subprojects {
    apply(plugin = "com.aliucord.plugin")
    apply(plugin = "com.android.library")
    apply(plugin = "org.jetbrains.kotlin.android")

    configure<AliucordExtension> {
        github(repositoryUrl)
    }

    configure<LibraryExtension> {
        namespace = projectNamespace
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

        compileOnly("com.aliucord:Aliucord:2.6.0")
        compileOnly("com.aliucord:Aliuhook:1.1.4")
        compileOnly("com.discord:discord:126021")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.20")
    }
}