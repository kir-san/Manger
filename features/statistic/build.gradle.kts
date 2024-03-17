plugins {
    id("compose.library")
}

android {
    namespace = "com.san.kir.statistic"
}

dependencies {
    implementation(project(Modules.Core.compose))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Data.db))

    implementation(libs.timber)
}
