plugins {
    id("compose.library")
    id(Plugins.parcelize)
}

android {
    namespace = "com.san.kir.data.models"
}

dependencies {
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Core.utils))

    implementation(libs.compose.runtime)
    implementation(libs.room.ktx)
    implementation(libs.gson)
}
