import org.gradle.api.JavaVersion

object Versions {
    object App {
        const val VERSION_CODE = 2711
        const val VERSION_NAME = "2.7.11"
        const val MIN_SDK = 21
        const val TARGET_SDK = 30
        const val COMPILE_SDK = 31
    }

    object Kotlin {
        // Make sure to update `buildSrc/build.gradle.kts` when updating this
        const val STDLIB = "1.5.31"
        const val COROUTINES = "1.5.1-native-mt"
        const val KSP = "1.5.31-1.0.0"
    }

    object AndroidX {
        const val CORE = "1.6.0"
        const val SPLASH = "1.0.0-alpha02"
        const val APPCOMPAT = "1.4.0-alpha03"
        const val LIFECYCLE = "2.3.1"
        const val VECTORDRAWABLE = "1.2.0-alpha02"
        const val CONSTRAINTLAYOUT = "2.0.0-beta4"
        const val WORKMANAGER = "2.7.0"
        const val HILT = "1.0.0"
        const val DATASTORE = "1.0.0"
        const val ROOM = "2.4.0-beta01"
        const val NAVIGATION = "2.4.0-alpha10"
    }

    object Compose {
        const val COMPOSE = "1.0.4"
        const val CONSTRAINT_LAYOUT = "1.0.0-beta02"
        const val HILT_NAVIGATION_COMPOSE = "1.0.0-alpha03"
        const val PAGING = "1.0.0-alpha14"
    }

    object Google {
        const val MATERIAL = "1.3.0"
        const val PROTOBUF_JAVALITE = "3.18.0"
        const val PROTOBUF_PROTOC = "3.18.0"
        const val PROTOBUF_PLUGIN = "0.8.17"
        const val ACCOMPANIST = "0.19.0"
        const val HILT = "2.37"
    }

    object Test {
        const val JUNIT = "4.13.2"
        const val TEST_CORE = "1.4.0"
        const val TEST_RULES = TEST_CORE
        const val TEST_JUNIT = "1.1.3"
        const val TEST_RUNNER = TEST_CORE
        const val TRUTH = "1.0.1"
        const val BENCHMARK_JUNIT = "1.0.0"
        const val KAKAOCUP = "0.0.2"
        const val ESPRESSO = "3.4.0"
    }

    const val HYPERION = "0.9.31"

    // Make sure to update `buildSrc/build.gradle.kts` when updating this
    const val GRADLE = "7.1.0-beta03"

    val JAVA = JavaVersion.VERSION_1_8

    object ForInternet {
        const val JSOUP = "1.13.1"
        const val GSON = "2.8.2"
        const val OKIO = "3.0.0"
        const val RETROFIT = "2.9.0"
        const val OKHTTP = "4.9.0"
    }
}
