version = "1.0.2" // Plugin version. Increment this to trigger the updater
description = "Better font scale" // Plugin description that will be shown to user

aliucord {
    // Changelog of your plugin
    changelog.set("""
        v1.0.2:
          - Added setting for username font scale
          - Added setting for tag font scale
          - Added setting for game/status font scale

        v1.0.1:
          - Added setting for chatbox font scale
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
