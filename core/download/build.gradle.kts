plugins {
    id("base.library")
    id(Plugins.kapt)
    id(Plugins.hilt)
}

android {
    namespace = "com.san.kir.core.download"
}

dependencies {
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Data.db))
    implementation(project(Modules.Core.internet))
    implementation(project(Modules.Data.parsing))

    implementation(libs.core)

    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.service)

    implementation(libs.timber)

    implementation(libs.hilt.android)
    kapt(libs.bundles.hiltCompilers)
}
