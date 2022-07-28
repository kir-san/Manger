import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    id("base.library")
    alias(libs.plugins.kotlin.ksp)
    id(Plugins.parcelize)
}

android {
    defaultConfig {
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
}

dependencies {
    implementation(project(Modules.Data.parsing))
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.support))

    implementation(libs.hilt.android)

    ksp(libs.room.compiler)
    api(libs.room.runtime)
    implementation(libs.bundles.room)

    implementation(libs.paging)

    implementation(libs.gson)
    implementation(libs.timber)
}
