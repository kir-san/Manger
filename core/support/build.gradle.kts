plugins {
    id("base.library")
}

android {
    namespace = "com.san.kir.core.support"
}

dependencies {
    implementation(libs.compose.ui)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material.icons.ext)
}
