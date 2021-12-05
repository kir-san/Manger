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
    implementation(project(Modules.Core.utils))

    Dependencies.Kotlin.apply {
        implementation(COROUTINES_CORE)
    }

    Dependencies.ForInternet.apply {
        implementation(JSOUP)
//        implementation(GSON)
        implementation(OKIO)
//        implementation(RETROFIT)
        implementation(OKHTTP)
    }

    implementation(Dependencies.INJECT)

    Dependencies.Test.apply {
//        testImplementation(JUNIT)
//        androidTestImplementation(TEST_CORE)
//        androidTestImplementation(TEST_RULES)
        androidTestImplementation(TEST_JUNIT)
//        androidTestImplementation(TEST_RUNNER)
//        androidTestImplementation(TRUTH)
//        androidTestImplementation(BENCHMARK_JUNIT)
//        androidTestImplementation(COMPOSE_JUNIT)
//        androidTestImplementation(KAKAOCUP)
//        androidTestImplementation(ESPRESSO)
//        androidTestImplementation(NAVIGATION)
//        debugImplementation(COMPOSE_MANIFEST)
    }
}
