import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.DependencyHandlerScope

fun Project.androidComposeLibraryConfig(): Unit =
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
        prepareComposeConfig()
    }
