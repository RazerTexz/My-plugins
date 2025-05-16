version = "1.0.8"
description = "More control over font sizes"

aliucord {
    changelog.set("""
        # 1.0.7
        * Improved patches
        * Removed Tag font scale setting

        # 1.0.6
        * Added a setting for Profile Status font scale

        # 1.0.5
        * Added a setting for About Me font scale

        # 1.0.4
        * Rewrote code from Java to Kotlin (pain)

        # 1.0.3
        * Fixed crashes caused by entering non-numeric values in plugin settings

        # 1.0.2
        * Added settings for Username font scale, Tag font scale, and Game Status font scale

        # 1.0.1
        * Added a setting for Chatbox font scale

        # 1.0.0
        * Initial version
    """.trimIndent())

    excludeFromUpdaterJson.set(false)
}
