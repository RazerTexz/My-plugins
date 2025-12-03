import com.android.build.gradle.LibraryExtension 

version = "1.0.3"
description = "(A.S.S) Style Aliucord to your liking!"

aliucord {
    changelog = """
        # 1.0.0
        * Initial version
    """.trimIndent()

    deploy = true
}

configure<LibraryExtension> {
    defaultConfig {
        minSdk = 26
    }
}