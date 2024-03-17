plugins {
    id("compose.app")
    id(Plugins.kapt)
    id(Plugins.parcelize)
}

android {
    namespace = "com.san.kir.manger"

    defaultConfig {
        applicationId = "com.san.kir.manger"

        versionCode = 283
        versionName = "2.8.3"

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

    kapt {
        correctErrorTypes = true
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
    implementation(project(Modules.Features.shikimori))
    implementation(project(Modules.Features.chapters))
    implementation(project(Modules.Features.library))
    implementation(project(Modules.Features.categories))
    implementation(project(Modules.Features.statistic))
    implementation(project(Modules.Features.storage))
    implementation(project(Modules.Features.settings))
    implementation(project(Modules.Features.schedule))
    implementation(project(Modules.Features.catalog))

    implementation(project(Modules.Features.Catalogs.allhen))

    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Core.internet))
    implementation(project(Modules.Core.compose))
    implementation(project(Modules.Core.background))

    implementation(project(Modules.Data.db))

    implementation(libs.activity)
    implementation(libs.appcompat)
    implementation(libs.vectordrawable)

    implementation(libs.bundles.decompose)
    implementation(libs.lifecycle.viewmodel)

    //    debugImplementation(libs.bundles.hyper)

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
