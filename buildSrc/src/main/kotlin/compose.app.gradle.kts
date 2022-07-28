import org.gradle.kotlin.dsl.extra

plugins {
    id("base.app")
}

android {
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.Compose.COMPOSE_COMPILER
    }
}
