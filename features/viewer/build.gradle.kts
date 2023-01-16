plugins {
    id("base.library")
    id(Plugins.hilt)
    id(Plugins.kapt)
    id(Plugins.parcelize)
}

android {
    namespace = "com.san.kir.features.viewer"

    buildFeatures {
        viewBinding = true
    }

    kapt {
        useBuildCache = true
    }
}

dependencies {
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Data.db))
    implementation(project(Modules.Data.parsing))
    implementation(project(Modules.Core.internet))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.support))

    implementation(libs.subsampling)

    implementation(libs.core)
    implementation(libs.activity)
    implementation(libs.fragment)
    implementation(libs.appcompat)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.common)

    implementation(libs.material)

    implementation(libs.timber)

    implementation(libs.hilt.android)
    kapt(libs.bundles.hiltCompilers)
}
