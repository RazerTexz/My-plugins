rootProject.name = "Aliucord Plugins"

// This file sets what projects are included. Every time you add a new project, you must add it
// to the includes below.

// Plugins are included like this
include(
    "Command"
    //"Testing"
)

// This is required because plugins are in the ExamplePlugins/kotlin subdirectory.
//
// Assuming you put all your plugins into the project root, so on the same
// level as this file, simply remove everything below.
//
// Otherwise, if you want a different structure, for example all plugins in a folder called "plugins",
// then simply change the path
rootProject.children.forEach {
    it.projectDir = file("Test/${it.name}")
}
