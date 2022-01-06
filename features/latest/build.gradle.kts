plugins {
    id(Plugins.ANDROID_LIBRARY)
    id(Plugins.KOTLIN_ANDROID)
    kotlin(Plugins.KAPT)
    id(Plugins.HILT_ANDROID)
}

androidComposeLibraryConfig()

dependencies {
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Data.db))
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.download))
    implementation(project(Modules.Core.composeUtils))

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

        implementation(HILT_NAVIGATION)
    }

    Dependencies.AndroidX.Lifecycle.apply {
        implementation(LIFECYCLE_LIVEDATA)
        implementation(LIFECYCLE_VIEWMODEL)
    }

    Dependencies.AndroidX.WorkManager.apply {
        implementation(WORK_RUNTIME)
        implementation(WORK_GCM)
        implementation(WORK_MULTIPROCESS)
    }

    Dependencies.Google.Hilt.apply {
        implementation(HILT_ANDROID)
        kapt(HILT_COMPILER)
    }

    Dependencies.AndroidX.Hilt.apply {
        implementation(HILT_WORK)
        kapt(HILT_COMPILER)
    }
}
