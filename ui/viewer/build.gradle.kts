plugins {
    id(Plugins.ANDROID_LIBRARY)
    id(Plugins.KOTLIN_ANDROID)
    id(Plugins.HILT_ANDROID)
    kotlin(Plugins.KAPT)
    id(Plugins.PARCELIZE)
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(Modules.Data.models))
    implementation(project(Modules.Data.store))
    implementation(project(Modules.Data.db))
    implementation(project(Modules.Core.internet))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.support))

    implementation("com.davemorrissey.labs:subsampling-scale-image-view-androidx:3.10.0")

    Dependencies.AndroidX.apply {
        implementation(CORE)
        implementation(EXIF)
        implementation(ACTIVITY)
        implementation(FRAGMENT)
        implementation(APPCOMPAT)
        implementation(VIEWPAGER2)
        implementation(CONSTRAINTLAYOUT)
    }

    Dependencies.AndroidX.Lifecycle.apply {
        implementation(LIFECYCLE_VIEWMODEL)
        implementation(LIFECYCLE_RUNTIME)
        implementation(LIFECYCLE_COMMON)
    }

    Dependencies.Google.apply {
        implementation(MATERIAL)
    }

    Dependencies.Google.Hilt.apply {
        implementation(HILT_ANDROID)
        kapt(HILT_COMPILER)
    }

    Dependencies.AndroidX.Hilt.apply {
        kapt(HILT_COMPILER)
    }

    Dependencies.AndroidX.Room.apply {
        implementation(ROOM_RUNTIME)
    }
}