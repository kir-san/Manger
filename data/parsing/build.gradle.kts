plugins {
    id("base.library")
}

dependencies {
    implementation(project(Modules.Core.internet))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Data.models))

    implementation(libs.hilt.inject)

    implementation(libs.gson)
    implementation(libs.coroutines.core)
    implementation(libs.timber)
}
