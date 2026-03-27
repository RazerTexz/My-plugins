import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

import com.android.build.gradle.LibraryExtension
import com.aliucord.gradle.AliucordExtension

val projectNamespace = "com.github.razertexz"
val repositoryUrl = "https://github.com/RazerTexz/My-plugins"

subprojects {
    apply(plugin = "com.aliucord.plugin")
    apply(plugin = "com.android.library")
    apply(plugin = "org.jetbrains.kotlin.android")

    configure<KotlinAndroidExtension> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
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

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }

    configure<AliucordExtension> {
        github(repositoryUrl)
    }

    dependencies {
        val compileOnly by configurations

        compileOnly("com.aliucord:Aliucord:2.6.0")
        compileOnly("com.aliucord:Aliuhook:1.1.4")
        compileOnly("com.discord:discord:126021")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.20")
    }
}