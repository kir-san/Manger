import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id(Plugins.ANDROID_APPLICATION)
    id(Plugins.HILT_ANDROID)
    id(Plugins.KOTLIN_ANDROID)
    kotlin(Plugins.KAPT)
//    id(Plugins.KSP)
    id(Plugins.PARCELIZE)
    id(Plugins.PROTOBUF) version "0.8.17"
}

android {
    compileSdk = Versions.App.COMPILE_SDK

    defaultConfig {
        applicationId = "com.san.kir.manger"

        minSdk = Versions.App.MIN_SDK
        targetSdk = Versions.App.TARGET_SDK

        versionCode = Versions.App.VERSION_CODE
        versionName = Versions.App.VERSION_NAME

        setProperty("archivesBaseName", "Manger $versionName")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

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

    signingConfigs {
        create("release") {
            keyAlias = "kir-san"
            keyPassword = Private.KEY_PASSWORD
            storeFile = file("../Key.jks")
            storePassword = Private.KEYSTORE_PASSWORD
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("String", "EXAMPLE", "\"release\"")
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            extra["enableCrashlytics"] = false

            applicationIdSuffix = ".debug"
            buildConfigField("String", "EXAMPLE", "\"debug\"")
        }
        create("benchmark") {
            isMinifyEnabled = true
            isShrinkResources = true
            extra["enableCrashlytics"] = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")

            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = Versions.JAVA
        targetCompatibility = Versions.JAVA
    }

    kotlinOptions {
        jvmTarget = Versions.JAVA.toString()
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.Compose.COMPOSE
    }
}

dependencies {
    implementation(project(Modules.ankofork))

    Dependencies.Kotlin.apply {
        implementation(STDLIB)
        implementation(COROUTINES_CORE)
        implementation(COROUTINES_ANDROID)
    }

    Dependencies.AndroidX.apply {
        implementation(CORE)
        implementation(SPLASH)
        implementation(APPCOMPAT)
        implementation(COLLECTION)
        implementation(PREFERENCE)
        implementation(VECTORDRAWABLE)
        implementation(CONSTRAINTLAYOUT)
    }

    Dependencies.Google.apply {
        implementation(MATERIAL)
        implementation(PROTOBUF)
        implementation(PLAY_SERVICES_GCM)
    }

    Dependencies.Google.Hilt.apply {
        implementation(HILT_ANDROID)
        kapt(HILT_COMPILER)
    }

    Dependencies.AndroidX.Hilt.apply {
        implementation(HILT_WORK)
        kapt(HILT_COMPILER)
    }

    Dependencies.Compose.apply {
        implementation(UI)
        implementation(UI_TOOLING)
        implementation(UI_TOOLING_PREVIEW)
        implementation(RUNTIME)
        implementation(COMPILER)
        implementation(ANIMATION)
        implementation(FOUNDATION)
        implementation(FOUNDATION_LAYOUT)
        implementation(MATERIAL)
        implementation(MATERIAL_ICONS_CORE)
        implementation(MATERIAL_ICONS_EXTENDED)

        implementation(HILT_NAVIGATION)
        implementation(PAGING_COMPOSE)
    }

    Dependencies.Google.Accompanist.apply {
        implementation(FLOWLAYOUT)
        implementation(PAGER)
        implementation(PAGER_INDICATORS)
        implementation(PERMISSIONS)
        implementation(SYSTEMUICONTROLLER)
        implementation(INSETS)
        implementation(INSETS_UI)
        implementation(NAVIGATION_ANIMATION)
    }

    Dependencies.AndroidX.Room.apply {
        kapt(ROOM_COMPILER)
        implementation(ROOM_RUNTIME)
        implementation(ROOM_KTX)
    }

    Dependencies.AndroidX.Lifecycle.apply {
        implementation(LIFECYCLE_VIEWMODEL)
        implementation(LIFECYCLE_RUNTIME)
        implementation(LIFECYCLE_LIVEDATA)
        implementation(LIFECYCLE_COMMON)
        implementation(LIFECYCLE_PROCESS)
        implementation(LIFECYCLE_SERVICE)
    }

    Dependencies.AndroidX.WorkManager.apply {
        implementation(WORK_RUNTIME)
        implementation(WORK_GCM)
        implementation(WORK_MULTIPROCESS)
    }

    Dependencies.Kittinunf.apply {
        implementation(FUEL)
        implementation(RESULT)
        implementation(RESULT_COROUTINES)
    }

    Dependencies.AndroidX.Datastore.apply {
        implementation(DATASTORE)
    }

    Dependencies.Other.apply {
        implementation(JSOUP)
        implementation(ANDROID_JOB)
        implementation(PROGRESSBUTTON)
    }


    //Big Image Viewer
    //implementation("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0")


    debugImplementation("com.willowtreeapps.hyperion:hyperion-core:0.9.31")
    debugImplementation("com.willowtreeapps.hyperion:hyperion-crash:0.9.31")
//    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")

    // Use the most recent version of Compose available.
    // debugImplementation 'org.jetbrains.kotlin:kotlin-reflect:1.5.20'
    testImplementation("junit:junit:4.12")

    androidTestImplementation("androidx.test:core:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("com.google.truth:truth:1.0.1")
    androidTestImplementation("androidx.benchmark:benchmark-junit4:1.0.0")

    // Test rules and transitive dependencies:
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Versions.Compose.COMPOSE}")
// Needed for createComposeRule, but not createAndroidComposeRule:
    debugImplementation("androidx.compose.ui:ui-test-manifest:${Versions.Compose.COMPOSE}")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.18.0"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins.create("java") {
                option("lite")
            }
        }
    }
}
