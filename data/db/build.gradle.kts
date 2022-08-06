plugins {
    id("base.library")
    alias(libs.plugins.kotlin.ksp)
    id(Plugins.parcelize)
    alias(libs.plugins.serialization)
}

android {
    defaultConfig {
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            allWarningsAsErrors = true
            allowSourcesFromOtherPlugins = true
        }
    }
}

dependencies {
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.support))

    implementation(libs.hilt.android)

    ksp(libs.room.compiler)
    api(libs.room.runtime)
    implementation(libs.bundles.room)

    implementation(libs.paging)

    implementation(libs.serialization)
    implementation(libs.timber)
}
