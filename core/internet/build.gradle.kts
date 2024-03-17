plugins {
    id("base.library")
}

android {
    namespace = "com.san.kir.core.internet"
}

dependencies {
    implementation(project(Modules.Core.utils))

    implementation(libs.coroutines.core)
    implementation(libs.stdlib)

    api(libs.jsoup)
    api(libs.okio)
    api(libs.okhttp)
    implementation(libs.okhttp.loging)

    api(libs.bundles.ktor)

    implementation(libs.compose.runtime)
    implementation(libs.timber)

//        testImplementation(JUNIT)
//        androidTestImplementation(TEST_CORE)
//        androidTestImplementation(TEST_RULES)
    androidTestImplementation(libs.test.junit)
//        androidTestImplementation(TEST_RUNNER)
//        androidTestImplementation(TRUTH)
//        androidTestImplementation(BENCHMARK_JUNIT)
//        androidTestImplementation(COMPOSE_JUNIT)
//        androidTestImplementation(KAKAOCUP)
//        androidTestImplementation(ESPRESSO)
//        androidTestImplementation(NAVIGATION)
//        debugImplementation(COMPOSE_MANIFEST)
}
