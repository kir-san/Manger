plugins {
    id(Plugins.ANDROID_LIBRARY)
    id(Plugins.KOTLIN_ANDROID)
    kotlin(Plugins.KAPT)
    id(Plugins.HILT_ANDROID)
}

androidComposeLibraryConfig()

dependencies {
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.composeUtils))
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Core.download))
    implementation(project(Modules.Core.background))
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Data.store))
    implementation(project(Modules.Data.db))
    implementation(project(Modules.Data.parsing))
    implementation(project(Modules.Features.viewer))

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
//
        implementation(HILT_NAVIGATION)
    }
//
    Dependencies.Google.Accompanist.apply {
        implementation(PAGER)
        implementation(PAGER_INDICATORS)
        implementation(INSETS)
        implementation(INSETS_UI)
    }

    Dependencies.AndroidX.Lifecycle.apply {
        implementation(LIFECYCLE_VIEWMODEL)
    }
//
    Dependencies.Google.Hilt.apply {
        implementation(HILT_ANDROID)
        kapt(HILT_COMPILER)
    }

    Dependencies.AndroidX.Hilt.apply {
        kapt(HILT_COMPILER)
    }

    Dependencies.AndroidX.WorkManager.apply {
        implementation(WORK_RUNTIME)
    }
}
