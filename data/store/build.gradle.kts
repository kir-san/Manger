import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import com.google.protobuf.gradle.plugins

plugins {
    id(Plugins.ANDROID_LIBRARY)
    id(Plugins.KOTLIN_ANDROID)
    kotlin(Plugins.KAPT)
    id(Plugins.PROTOBUF) version Versions.Google.PROTOBUF_PLUGIN
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
    implementation(project(Modules.Data.models))

    Dependencies.AndroidX.Datastore.apply {
        implementation(DATASTORE)
    }

    Dependencies.Google.apply {
//        implementation(PROTOBUF_JAVALITE)
    }

    implementation("com.google.protobuf:protobuf-kotlin:${Versions.Google.PROTOBUF}")

    Dependencies.Google.Hilt.apply {
        implementation(HILT_ANDROID)
//        kapt(HILT_COMPILER)
    }

    implementation(Dependencies.INJECT)
}

protobuf {
    protoc {
        artifact = Dependencies.Google.PROTOBUF_PROTOC
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins.create("java") {
//                option("lite")
            }
            task.builtins {
                id("kotlin")
            }
        }
    }
}
