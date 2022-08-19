plugins {
    id("compose.library")
    id(Plugins.kapt)
    id(Plugins.hilt)
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Data.db))
    implementation(project(Modules.Core.composeUtils))
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Core.internet))

    implementation(libs.okhttp.loging)

    implementation(libs.core)
    implementation(libs.activity)
    implementation(libs.appcompat)

    implementation(libs.material)

    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.common)
    implementation(libs.lifecycle.viewmodel)

    implementation(libs.compose.hilt.navigation)
    implementation(libs.hilt.android)
    kapt(libs.bundles.hiltCompilers)

    implementation(libs.timber)
    implementation(libs.gson)
    implementation(libs.datastore)
}
