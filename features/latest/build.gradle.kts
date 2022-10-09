plugins {
    id("compose.library")
    id(Plugins.kapt)
    id(Plugins.hilt)
}

android {
    namespace = "com.san.kir.features.latest"
}

dependencies {
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Data.db))
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.download))
    implementation(project(Modules.Core.composeUtils))

    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.bundles.work)
    implementation(libs.compose.hilt.navigation)
    implementation(libs.bundles.hilt)
    kapt(libs.bundles.hiltCompilers)
}
