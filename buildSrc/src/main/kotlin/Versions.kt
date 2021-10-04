import org.gradle.api.JavaVersion

object Versions {
    object App {
        const val VERSION_CODE = 280
        const val VERSION_NAME = "2.8.0"
        const val MIN_SDK = 21
        const val TARGET_SDK = 30
        const val COMPILE_SDK = 31
    }

    object Kotlin {
        // Make sure to update `buildSrc/build.gradle.kts` when updating this
        const val STDLIB = "1.5.21"
        const val COROUTINES = "1.5.1-native-mt"
    }

    object AndroidX {
        const val CORE = "1.6.0"
        const val APPCOMPAT = "1.4.0-alpha03"
        const val COLLECTION = "1.1.0"
        const val LIFECYCLE = "2.3.1"
        const val PREFERENCE = "1.1.1"
        const val VECTORDRAWABLE = "1.2.0-alpha02"
        const val CONSTRAINTLAYOUT = "2.0.0-beta4"
        const val WORKMANAGER = "2.7.0-beta01"
        const val HILT = "1.0.0"
        const val DATASTORE = "1.0.0"
        const val ROOM = "2.3.0"
    }

    object Compose {
        const val COMPOSE = "1.0.2"
        const val HILT_NAVIGATION_COMPOSE = "1.0.0-alpha03"
        const val PAGING = "1.0.0-alpha12"
    }

    object Google {
        const val MATERIAL = "1.3.0"
        const val PROTOBUF_JAVALITE = "3.11.0"
        const val PLAY_SERVICES_GCM = "17.0.0" // не менять версию
        const val ACCOMPANIST = "0.19.0"
        const val HILT = "2.37"
    }

    object Kittinunf {
        const val FUEL = "2.3.1"
        const val RESULT = "4.0.0"
    }

    // Make sure to update `buildSrc/build.gradle.kts` when updating this
    const val GRADLE = "7.1.0-alpha10"

    val JAVA = JavaVersion.VERSION_1_8

    const val JSOUP = "1.13.1"
    const val ANDROID_JOB = "1.4.2" // на удаление
    const val PROGRESSBUTTON = "2.0.1" // на удаление
}
