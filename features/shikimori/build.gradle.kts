plugins {
    id("compose.library")
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.san.kir.features.shikimori"
}

dependencies {
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Data.db))
    implementation(project(Modules.Core.compose))
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

    implementation(libs.timber)
    implementation(libs.datastore)
}
