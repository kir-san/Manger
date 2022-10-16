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
    implementation(project(Modules.Core.internet))
    implementation(project(Modules.Data.db))
    implementation(project(Modules.Data.parsing))

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    implementation(libs.timber)

    implementation(libs.compose.hilt.navigation)
    implementation(libs.hilt.android)
    kapt(libs.bundles.hiltCompilers)
}
