plugins {
    id("compose.app")
    id(Plugins.hilt)
    id(Plugins.kapt)
    alias(libs.plugins.kotlin.ksp)
    id(Plugins.parcelize)
}

android {
    defaultConfig {
        applicationId = "com.san.kir.manger"

        versionCode = 2715
        versionName = "2.8"

        setProperty("archivesBaseName", "Manger $versionName")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
//                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
//                    "room.expandProjection" to "true"
                )
            }
        }
    }

//    flavorDimensions += "version"
//    productFlavors {
//        create("r") {
//            dimension = "version"
//            applicationIdSuffix = ""
//            versionNameSuffix = ""
//        }
//        create("alpha") {
//            dimension = "version"
//            applicationIdSuffix = ".alpha"
//            versionNameSuffix = "-alpha"
//        }
//    }
}

dependencies {
    implementation(project(Modules.Features.viewer))
    implementation(project(Modules.Features.latest))
    implementation(project(Modules.Features.shikimori))
    implementation(project(Modules.Features.chapters))
    implementation(project(Modules.Features.library))
    implementation(project(Modules.Features.categories))
    implementation(project(Modules.Features.statistic))

    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Core.download))
    implementation(project(Modules.Core.internet))
    implementation(project(Modules.Core.composeUtils))
    implementation(project(Modules.Core.background))

    implementation(project(Modules.Data.db))
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Data.parsing))

    implementation(libs.stdlib)
    implementation(libs.bundles.coroutines)

    implementation(libs.core)
    implementation(libs.splash)
    implementation(libs.activity)
    implementation(libs.appcompat)
    implementation(libs.vectordrawable)

    implementation(libs.material)

    implementation(libs.bundles.hilt)
    kapt(libs.bundles.hiltCompilers)

    implementation(libs.compose.constraint.layout)
    implementation(libs.compose.hilt.navigation)
    implementation(libs.compose.paging)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.common)
    implementation(libs.lifecycle.service)

    implementation(libs.bundles.work)

    debugImplementation(libs.bundles.hyper)

    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.tests)
    debugImplementation(libs.compose.manifest)

//        androidTestImplementation(TRUTH)
//        androidTestImplementation(BENCHMARK_JUNIT)
//        androidTestImplementation(NAVIGATION)
//    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")

    // Use the most recent version of Compose available.
    // debugImplementation 'org.jetbrains.kotlin:kotlin-reflect:1.5.20'
}
