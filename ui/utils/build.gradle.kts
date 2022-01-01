plugins {
    id(Plugins.ANDROID_LIBRARY)
    id(Plugins.KOTLIN_ANDROID)
}

androidComposeLibraryConfig()

dependencies {
    implementation(project(Modules.Core.utils))

    Dependencies.Compose.apply {
        implementation(UI)
        implementation(UI_TOOLING)
        implementation(UI_TOOLING_PREVIEW)
        implementation(RUNTIME)
        implementation(COMPILER)
        implementation(ANIMATION)
        implementation(FOUNDATION)
        implementation(FOUNDATION_LAYOUT)
        implementation(MATERIAL)
        implementation(MATERIAL_ICONS_CORE)
        implementation(MATERIAL_ICONS_EXTENDED)
    }

    Dependencies.Google.Accompanist.apply {
        implementation(INSETS)
        implementation(INSETS_UI)
        implementation(NAVIGATION_ANIMATION)
    }
}
