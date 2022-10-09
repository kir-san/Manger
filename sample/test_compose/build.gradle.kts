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
    api(libs.bundles.compose)
    api(libs.bundles.accompanist)
}
