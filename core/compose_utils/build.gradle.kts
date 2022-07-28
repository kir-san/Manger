plugins {
    id("compose.library")
}

dependencies {
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.internet))

    api(libs.bundles.compose)
    api(libs.bundles.accompanist)
    implementation(libs.core)
    implementation(libs.timber)
}
