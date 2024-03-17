plugins {
    id("base.library")
    id(Plugins.kotlin)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.san.kir.core.utils"
}

dependencies {
    implementation(project(Modules.Core.support))

    implementation(libs.compose.runtime)
    implementation(libs.compose.runtime.saveable)
    implementation(libs.compose.ui)
    implementation(libs.serialization)

    api(libs.bundles.coroutines)
    api(libs.decompose)
    api(libs.decompose.extensions)

    implementation(libs.stdlib)
    implementation(libs.timber)
    implementation(libs.lifecycle.viewmodel)
}
