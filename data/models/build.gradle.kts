plugins {
    id("base.library")
    id(Plugins.parcelize)
}

dependencies {
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Core.utils))

    implementation(libs.room.ktx)
    implementation(libs.gson)
}
