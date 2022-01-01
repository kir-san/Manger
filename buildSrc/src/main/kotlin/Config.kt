
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

fun Project.androidLibraryConfig(): Unit =
    (this as ExtensionAware).extensions.configure<LibraryExtension>("android") {
        compileSdk = Versions.App.COMPILE_SDK

        defaultConfig {
            minSdk = Versions.App.MIN_SDK
            targetSdk = Versions.App.TARGET_SDK

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

fun TestedExtension.prepareKotlinOptions(): Unit =
    (this as ExtensionAware).extensions.configure<KotlinJvmOptions>("kotlinOptions") {
        jvmTarget = Versions.JAVA.toString()
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }

// --- For Compose --
fun LibraryExtension.prepareComposeConfig() {
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.Compose.COMPOSE
    }
}

fun BaseAppModuleExtension.prepareComposeConfig() {
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.Compose.COMPOSE
    }
}
// --- For Compose ---

val resourceExcludes = listOf(
    "META-INF/**/*",
    "META-INF/DEPENDENCIES",
    "META-INF/LICENSE",
    "META-INF/LICENSE.txt",
    "META-INF/license.txt",
    "META-INF/NOTICE",
    "META-INF/NOTICE.txt",
    "META-INF/notice.txt",
    "META-INF/ASL2.0",
    "META-INF/AL2.0",
    "META-INF/LGPL2.1",
    "META-INF/*.kotlin_module"
)
