plugins {
    id("compose.library")
    id(Plugins.kapt)
    id(Plugins.hilt)
}

android {
    namespace="com.san.kir.statistic"
}

dependencies {
    implementation(project(Modules.Core.compose))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Data.db))

    implementation(libs.timber)

    implementation(libs.compose.hilt.navigation)
    implementation(libs.hilt.android)
    kapt(libs.bundles.hiltCompilers)

}
