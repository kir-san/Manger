import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("com.android.library")
    id("basic")
}

kotlin {
    explicitApi = ExplicitApiMode.Strict
}


android {
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kotlin {
        jvmToolchain(libs.versions.java.get().toInt())

        buildTypes.onEach { variant ->
            sourceSets {
                getByName(variant.name) {
                    kotlin.srcDir(layout.buildDirectory.dir("/generated/ksp/${variant.name}/kotlin"))
                }
            }
        }
    }
}
