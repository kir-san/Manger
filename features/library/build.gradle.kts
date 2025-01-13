plugins {
    id("compose.library")
//    alias(libs.plugins.vkompose)
//    id("com.github.takahirom.decomposer")
}

android {
    namespace = "com.san.kir.library"
}

dependencies {
    implementation(projects.core.compose)
    implementation(projects.core.utils)
    implementation(projects.core.background)
    implementation(projects.data.db)
    implementation(projects.data.parsing)

    implementation(projects.features.chapters)
    implementation(projects.features.catalog)
    implementation(projects.features.categories)
    implementation(projects.features.statistic)
    implementation(projects.features.storage)
    implementation(projects.features.settings)
    implementation(projects.features.schedule)
    implementation(projects.features.accounts.main)

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
