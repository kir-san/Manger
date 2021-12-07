plugins {
    id(Plugins.ANDROID_LIBRARY)
    id(Plugins.KOTLIN_ANDROID)
//    id(Plugins.HILT_ANDROID)
    id(Plugins.KSP) version Versions.Kotlin.KSP
    id(Plugins.PARCELIZE)
//    kotlin(Plugins.KAPT)
}

android {
    compileSdk = Versions.App.COMPILE_SDK

    defaultConfig {
        minSdk = Versions.App.MIN_SDK
        targetSdk = Versions.App.TARGET_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(project(Modules.Data.parsing))
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.support))

    Dependencies.Google.Hilt.apply {
        implementation(HILT_ANDROID)
//        kapt(HILT_COMPILER)
    }

    Dependencies.AndroidX.Hilt.apply {
//        kapt(HILT_COMPILER)
    }

    Dependencies.AndroidX.Room.apply {
        ksp(ROOM_COMPILER)
        implementation(ROOM_RUNTIME)
        implementation(ROOM_KTX)
        implementation(ROOM_PAGING)
    }

    Dependencies.AndroidX.Lifecycle.apply {
        implementation(LIFECYCLE_LIVEDATA)
    }

    Dependencies.AndroidX.apply {
        implementation(PAGING)
    }

}
