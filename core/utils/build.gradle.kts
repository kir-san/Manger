plugins {
    id("base.library")
    id(Plugins.kotlin)
}

dependencies {
    implementation(project(Modules.Core.support))

    implementation(libs.stdlib)
    api(libs.bundles.coroutines)
    implementation(libs.timber)
    implementation(libs.lifecycle.viewmodel)
}
