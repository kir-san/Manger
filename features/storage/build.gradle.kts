plugins {
    id("compose.library")
    `kotlin-parcelize`
//    alias(libs.plugins.vkompose)
}

android {
    namespace = "com.san.kir.storage"
}

dependencies {
    implementation(projects.core.compose)
    implementation(projects.core.utils)
    implementation(projects.core.background)
    implementation(projects.data.db)

    api(projects.ksp)
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
