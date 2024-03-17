plugins {
    id("compose.library")
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.san.kir.features.catalogs.allhen"
}

dependencies {
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.compose))
    implementation(project(Modules.Core.internet))
    implementation(project(Modules.Data.parsing))

    implementation(libs.lifecycle.viewmodel)

    implementation(libs.timber)
}
