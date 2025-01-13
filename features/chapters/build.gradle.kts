plugins {
    id("compose.library")
//    alias(libs.plugins.vkompose)
//    id("com.github.takahirom.decomposer")
}

android {
    namespace = "com.san.kir.chapters"
}

dependencies {
    implementation(projects.core.utils)
    implementation(projects.core.compose)
    implementation(projects.core.background)
    implementation(projects.core.internet)
    implementation(projects.data.db)
    implementation(projects.data.parsing)
    implementation(projects.features.viewer)
    implementation(projects.features.catalog)

    implementation(projects.ksp)
    ksp(projects.ksp)
}

//vkompose {
//    skippabilityCheck {
//        stabilityConfigurationPath = "${project.rootDir}/compose_stability_config.conf"
//        strongSkippingEnabled = true
//    }
//    recompose {
//        isHighlighterEnabled = true
//        isLoggerEnabled = true
//    }
//    testTag {
//        isApplierEnabled = true
//        isDrawerEnabled = true
//        isCleanerEnabled = false
//    }
//}
