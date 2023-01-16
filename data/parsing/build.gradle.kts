plugins {
    id("base.library")
}

android {
    namespace = "com.san.kir.data.parsing"
}

dependencies {
    implementation(project(Modules.Core.internet))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Data.db))

    implementation(libs.hilt.inject)

    implementation(libs.gson)
    implementation(libs.coroutines.core)
    implementation(libs.timber)
}
