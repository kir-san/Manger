plugins {
    id("base.library")
}

dependencies {
    implementation(project(Modules.Core.utils))

    implementation(libs.stdlib)

    implementation(libs.coroutines.core)

    api(libs.jsoup)
    api(libs.okio)
    api(libs.okhttp)

    api(libs.bundles.ktor)

    implementation(libs.compose.runtime)
    implementation(libs.hilt.inject)
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
