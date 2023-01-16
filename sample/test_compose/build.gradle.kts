plugins {
    id("compose.app")
}

android {
    namespace = "com.san.kir.test_compose"
    defaultConfig {
        applicationId = "com.san.kir.test_compose"

        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    api(libs.bundles.compose)
    api(libs.bundles.accompanist)
}
