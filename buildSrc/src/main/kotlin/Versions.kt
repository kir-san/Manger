import org.gradle.api.JavaVersion

object Versions {
    object App {
        const val VERSION_CODE = 2715
        const val VERSION_NAME = "2.8"
        const val MIN_SDK = 21
        const val TARGET_SDK = 30
        const val COMPILE_SDK = 32
    }

    object Kotlin {
        // Make sure to update `buildSrc/build.gradle.kts` when updating this
        const val STDLIB = "1.6.10"
        const val COROUTINES = "1.6.0"
        const val KSP = "1.6.10-1.0.2"
    }

    object AndroidX {
        const val CORE = "1.8.0"
        const val ACTIVITY = "1.4.0"
        const val FRAGMENT = "1.4.1"
        const val SPLASH = "1.0.0-alpha02"
        const val APPCOMPAT = "1.4.2"
        const val LIFECYCLE = "2.4.1"
        const val VECTORDRAWABLE = "1.2.0-beta01"
        const val CONSTRAINTLAYOUT = "2.1.4"
        const val PAGING = "3.1.1"
        const val WORKMANAGER = "2.7.1"
        const val HILT = "1.0.0"
        const val DATASTORE = "1.0.0"
        const val ROOM = "2.4.2"
        const val NAVIGATION = "2.4.2"
    }

    object Compose {
        const val COMPOSE = "1.1.1"
        const val CONSTRAINT_LAYOUT = "1.0.1"
        const val HILT_NAVIGATION_COMPOSE = "1.0.0"
        const val PAGING = "1.0.0-alpha15"
    }

    object Google {
        const val MATERIAL = "1.3.0"
        const val PROTOBUF = "3.18.0"
        const val PROTOBUF_PLUGIN = "0.8.17"
        const val ACCOMPANIST = "0.22.1-rc"
        const val HILT = "2.42"
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
    const val SUBSAMPLING = "3.10.0"
    const val TIMBER = "5.0.1"

    // Make sure to update `buildSrc/build.gradle.kts` when updating this
    const val GRADLE = "7.1.2"

    val JAVA = JavaVersion.VERSION_1_8

    object ForInternet {
        const val JSOUP = "1.13.1"
        const val GSON = "2.8.2"
        const val OKIO = "3.0.0"
        const val RETROFIT = "2.9.0"
        const val OKHTTP = "4.9.0"
    }
}
