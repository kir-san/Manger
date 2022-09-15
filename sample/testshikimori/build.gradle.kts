import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    id("base.app")
    id(Plugins.kapt)
    id(Plugins.hilt)
}

android {
    defaultConfig {
        applicationId = "com.san.kir.testshikimori"

        versionCode = 1
        versionName = "1.0"

        setProperty("archivesBaseName", "Test Shikimori $versionName")
    }
}

dependencies {
    implementation(project(Modules.Features.shikimori))
    implementation(project(Modules.Data.db))
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Core.utils))

    implementation(libs.core)
    implementation(libs.appcompat)
    implementation(libs.vectordrawable)
    implementation(libs.constraintlayout)

    implementation(libs.material)

    implementation(libs.hilt.android)
    kapt(libs.bundles.hiltCompilers)
}
