plugins {
    id("base.library")
    id(Plugins.kotlin)
}

android {
    namespace = "com.san.kir.core.utils"
}

dependencies {
    implementation(project(Modules.Core.support))

    implementation(libs.stdlib)
    api(libs.collections.immutable)
    api(libs.bundles.coroutines)
    implementation(libs.timber)
    implementation(libs.lifecycle.viewmodel)
}
