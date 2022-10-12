plugins {
    id("compose.library")
}

android {
    namespace = "com.san.kir.core.compose"
}

dependencies {
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.internet))

    api(libs.bundles.compose)
    api(libs.bundles.accompanist)
    implementation(libs.core)
    implementation(libs.timber)
}
