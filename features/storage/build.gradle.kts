plugins {
    id("compose.library")
}

android {
    namespace="com.san.kir.storage"
}

dependencies {
    implementation(project(Modules.Core.compose))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Core.background))
    implementation(project(Modules.Data.db))

    implementation(libs.timber)

    implementation(libs.lifecycle.livedata)
}
