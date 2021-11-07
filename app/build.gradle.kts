import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id(Plugins.ANDROID_APPLICATION)
    id(Plugins.HILT_ANDROID)
    id(Plugins.KOTLIN_ANDROID)
    kotlin(Plugins.KAPT)
    id(Plugins.KSP) version Versions.Kotlin.KSP
    id(Plugins.PARCELIZE)
    id(Plugins.PROTOBUF) version Versions.Google.PROTOBUF_PLUGIN
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

        resourceConfigurations.addAll(listOf("en", "ru"))
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

    packagingOptions {
        resources.excludes.add("META-INF/**/*")
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/LICENSE.txt")
        resources.excludes.add("META-INF/license.txt")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("META-INF/NOTICE.txt")
        resources.excludes.add("META-INF/notice.txt")
        resources.excludes.add("META-INF/ASL2.0")
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
        resources.excludes.add("META-INF/*.kotlin_module")
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
        implementation(PROTOBUF_JAVALITE)
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
        ksp(ROOM_COMPILER)
        implementation(ROOM_RUNTIME)
        implementation(ROOM_KTX)
        implementation(ROOM_PAGING)
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

    Dependencies.AndroidX.Datastore.apply {
        implementation(DATASTORE)
    }

    Dependencies.ForInternet.apply {
        implementation(JSOUP)
        implementation(GSON)
        implementation(OKIO)
//        implementation(RETROFIT)
        implementation(OKHTTP)
    }

    Dependencies.Hyperion.apply {
        debugImplementation(CORE)
        debugImplementation(CRASH)
    }

    Dependencies.Test.apply {
        testImplementation(JUNIT)
        androidTestImplementation(TEST_CORE)
        androidTestImplementation(TEST_RULES)
        androidTestImplementation(TEST_JUNIT)
        androidTestImplementation(TEST_RUNNER)
        androidTestImplementation(TRUTH)
        androidTestImplementation(BENCHMARK_JUNIT)
        androidTestImplementation(COMPOSE_JUNIT)
        androidTestImplementation(KAKAOCUP)
        androidTestImplementation(ESPRESSO)
        androidTestImplementation(NAVIGATION)
        debugImplementation(COMPOSE_MANIFEST)
    }

    //Big Image Viewer
    //implementation("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0")
//    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")

    // Use the most recent version of Compose available.
    // debugImplementation 'org.jetbrains.kotlin:kotlin-reflect:1.5.20'
}

protobuf {
    protoc {
        artifact = Dependencies.Google.PROTOBUF_PROTOC
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins.create("java") {
                option("lite")
            }
        }
    }
}
