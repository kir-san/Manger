import org.gradle.api.JavaVersion

object Versions {
    object App {
        const val MIN_SDK = 21
        const val TARGET_SDK = 33
        const val COMPILE_SDK = 33
    }

    object Compose {
        const val COMPOSE_COMPILER = "1.3.2"
    }

    object Google

    val JAVA = JavaVersion.VERSION_1_8
}
