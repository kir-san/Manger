import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("android")
}

androidConfig {

    plugins {
        alias(libs.plugins.serialization)
        alias(libs.plugins.kotlin.ksp)
    }

    compileSdk = libs.versions.complileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = projectJavaVersion
        targetCompatibility = projectJavaVersion
    }

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(projectJvmTarget)

            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${layout.projectDirectory}/compose_metrics"
            )
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${layout.projectDirectory}/compose_metrics"
            )
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=${project.rootDir}/compose_stability_config.conf"
            )
        }
    }
}

dependencies {
    implementation(libs.serialization)
    implementation(libs.timber)
    implementation(libs.datetime)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.decompose)
}
