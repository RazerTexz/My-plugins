import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.objectweb.asm.*

import com.android.build.gradle.LibraryExtension
import com.aliucord.gradle.AliucordExtension

subprojects {
    apply(plugin = "com.aliucord.plugin")
    apply(plugin = "com.android.library")
    apply(plugin = "org.jetbrains.kotlin.android")

    configure<KotlinAndroidExtension> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
            freeCompilerArgs.addAll("-Xno-param-assertions", "-Xno-call-assertions", "-Xno-receiver-assertions", "-Xno-source-debug-extension")
        }
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

    afterEvaluate {
        tasks.named<KotlinCompile>("compileDebugKotlin") {
            doLast {
                destinationDirectory.get().asFile.walk().filter { it.extension == "class" }.forEach {
                    val reader = ClassReader(it.readBytes())
                    val writer = ClassWriter(reader, 0)

                    reader.accept(object : ClassVisitor(Opcodes.ASM9, writer) {
                        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
                            return if (descriptor == "Lkotlin/Metadata;") null else super.visitAnnotation(descriptor, visible)
                        }
                    }, 0)

                    it.writeBytes(writer.toByteArray())
                }
            }
        }
    }

    dependencies {
        val compileOnly by configurations

        compileOnly("com.aliucord:Aliucord:2.6.0")
        compileOnly("com.aliucord:Aliuhook:1.1.4")
        compileOnly("com.discord:discord:126021")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
    }
}