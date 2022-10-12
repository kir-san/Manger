plugins {
    id("compose.library")
    id(Plugins.kapt)
    id(Plugins.hilt)
}

android {
    namespace = "com.san.kir.chapters"
}

dependencies {
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.compose))
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Core.download))
    implementation(project(Modules.Core.background))
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Data.db))
    implementation(project(Modules.Data.parsing))
    implementation(project(Modules.Features.viewer))

    implementation(libs.lifecycle.viewmodel)

    implementation(libs.timber)

    implementation(libs.compose.hilt.navigation)
    implementation(libs.hilt.android)
    kapt(libs.bundles.hiltCompilers)
}
