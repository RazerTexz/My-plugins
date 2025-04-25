version = "1.0.8" // Plugin version. Increment this to trigger the updater
description = "More control over font sizes" // Plugin description that will be shown to user

aliucord {
    // Changelog of your plugin
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
    // Image or Gif that will be shown at the top of your changelog page
    // changelogMedia.set("https://cool.png")

    // Add additional authors to this plugin
    // author("Name", 0)
    // author("Name", 0)

    // Excludes this plugin from the updater, meaning it won't show up for users.
    // Set this if the plugin is unfinished
    excludeFromUpdaterJson.set(false)
}
