plugins {
    id(Plugins.ANDROID_APPLICATION)
    id(Plugins.KOTLIN_ANDROID)
}

android {
    compileSdk = Versions.App.COMPILE_SDK

    defaultConfig {
        applicationId = "com.san.kir.test_compose"

        minSdk = Versions.App.MIN_SDK
        targetSdk = Versions.App.TARGET_SDK

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = "kir-san"
            keyPassword = Private.KEY_PASSWORD
            storeFile = file("../../Key.jks")
            storePassword = Private.KEYSTORE_PASSWORD
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")
            buildConfigField("String", "EXAMPLE", "\"release\"")
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            extra["enableCrashlytics"] = false

            applicationIdSuffix = ".debug"
            buildConfigField("String", "EXAMPLE", "\"debug\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    prepareKotlinOptions()
    prepareComposeConfig()
}

dependencies {

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
}
