plugins {
    id(Plugins.ANDROID_LIBRARY)
    id(Plugins.KOTLIN_ANDROID)
    kotlin(Plugins.KAPT)
    id(Plugins.HILT_ANDROID)
}

android {
    compileSdk = Versions.App.COMPILE_SDK

    defaultConfig {
        minSdk = Versions.App.MIN_SDK
        targetSdk = Versions.App.TARGET_SDK

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = Versions.JAVA
        targetCompatibility = Versions.JAVA
    }

    prepareKotlinOptions()
}

dependencies {
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Core.utils))
//    implementation(project(Modules.Core.support))
    implementation(project(Modules.Data.db))
//    implementation(project(Modules.Core.internet))
    implementation(project(Modules.Data.parsing))
//    implementation(project(Modules.Data.store))
//
//    Dependencies.AndroidX.apply {
//        implementation(CORE)
//    }
//
    Dependencies.AndroidX.Lifecycle.apply {
        implementation(LIFECYCLE_LIVEDATA)
//        implementation(LIFECYCLE_RUNTIME)
//        implementation(LIFECYCLE_SERVICE)
    }

    Dependencies.Kotlin.apply {
        implementation(COROUTINES_CORE)
        implementation(COROUTINES_ANDROID)
    }

    Dependencies.Google.Hilt.apply {
        implementation(HILT_ANDROID)
        kapt(HILT_COMPILER)
    }
    Dependencies.AndroidX.Hilt.apply {
        implementation(HILT_WORK)
        kapt(HILT_COMPILER)
    }

    Dependencies.AndroidX.WorkManager.apply {
        implementation(WORK_RUNTIME)
        implementation(WORK_GCM)
        implementation(WORK_MULTIPROCESS)
    }
}
