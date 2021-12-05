plugins {
    id(Plugins.ANDROID_LIBRARY)
    id(Plugins.KOTLIN_ANDROID)
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
    implementation(project(Modules.Core.internet))
    implementation(project(Modules.Core.utils))
    implementation(project(Modules.Core.support))
    implementation(project(Modules.Data.models))

    implementation(Dependencies.INJECT)

    Dependencies.ForInternet.apply {
        implementation(JSOUP)
        implementation(GSON)
        implementation(OKHTTP)
    }

    Dependencies.Kotlin.apply {
        implementation(COROUTINES_CORE)
    }
}
