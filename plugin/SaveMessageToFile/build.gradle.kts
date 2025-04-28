version = "1.1.3" // Plugin version. Increment this to trigger the updater
description = "Adds a new context menu for saving messages to a file" // Plugin description that will be shown to user

aliucord {
    // Changelog of your plugin
    changelog.set("""
        # 1.1.2
        * Fix crash

        # 1.1.1
        * Added 'Skip File Name Dialog' in settings tab

        # 1.1.0
        * Added settings tab

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
