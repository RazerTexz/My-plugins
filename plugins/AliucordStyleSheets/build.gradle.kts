import com.android.build.gradle.LibraryExtension 

version = "1.0.7"
description = "(A.S.S) Style Aliucord to your liking!"

aliucord {
    changelog = """
        # 1.0.0
        * Initial version
    """.trimIndent()

    author("RazerTexz", 633565155501801472L)
}

configure<LibraryExtension> {
    defaultConfig {
        minSdk = 26
    }
}