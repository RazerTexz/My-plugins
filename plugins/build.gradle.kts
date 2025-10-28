import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension
import com.android.build.gradle.LibraryExtension
import com.aliucord.gradle.AliucordExtension

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "org.jetbrains.kotlin.android")
    apply(plugin = "com.aliucord.plugin")

    configure<KotlinAndroidExtension> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    configure<LibraryExtension> {
        namespace = "com.github.razertexz"
        compileSdk = 36

        defaultConfig {
            minSdk = 21
        }

        buildFeatures {
            buildConfig = true
            shaders = false
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    configure<AliucordExtension> {
        author("RazerTexz", 633565155501801472L)
        github("https://github.com/RazerTexz/My-plugins")
    }

    dependencies {
        val compileOnly by configurations

        compileOnly("com.aliucord:Aliucord:2.4.0") // 2.5.0 is broken
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
        compileOnly("com.discord:discord:126021")
    }
}