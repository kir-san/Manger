plugins {
    id("compose.library")
    id(Plugins.kapt)
    id(Plugins.hilt)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.san.kir.features.catalogs.allhen"
}

dependencies {
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.compose))
    implementation(project(Modules.Core.internet))
    implementation(project(Modules.Data.parsing))

    implementation(libs.lifecycle.viewmodel)

    implementation(libs.compose.hilt.navigation)
    implementation(libs.hilt.android)
    kapt(libs.bundles.hiltCompilers)

    implementation(libs.timber)
}
