import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension
import com.android.build.gradle.LibraryExtension
import com.aliucord.gradle.AliucordExtension

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
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }

    configure<AliucordExtension> {
        author("RazerTexz", 633565155501801472L)
        githubUrl = "https://github.com/RazerTexz/My-plugins"
        updateUrl = "https://raw.githubusercontent.com/RazerTexz/My-plugins/builds/updater.json"
        buildUrl = "https://raw.githubusercontent.com/RazerTexz/My-plugins/builds/${project.name}.zip"
    }

    dependencies {
        val compileOnly by configurations

        compileOnly("com.aliucord:Aliucord:2.6.0")
        compileOnly("com.aliucord:Aliuhook:1.1.4")
        compileOnly("com.discord:discord:126021")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
    }
}