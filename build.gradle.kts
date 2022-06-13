// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.github.ivancarras.graphfity") version "1.0.0"
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.GRADLE}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.STDLIB}")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
        classpath("com.google.dagger:hilt-android-gradle-plugin:${Versions.Google.HILT}")
        classpath("com.github.ivancarras:graphfity-plugin:1.0.0")
    }
}

//plugins {
//    id("com.osacky.doctor") version "0.7.0"
//}

allprojects {
    repositories {
        mavenCentral()
//        maven { url "https://jitpack.io" }
        google()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

configure<com.github.ivancarras.graphfity.plugin.main.GraphfityPluginExtension> {
    nodeTypesPath.set("nodesTypes.json") //(mandatory) Examples: graphfityConfig/nodesTypes.json establish the route to your nodeTypes.json
    projectRootName.set(":app") //(optional) Examples: ":app", ":feature:wishlist"... is up to you
//    graphImagePath.set("<graphsFolder>") //(optional)the folder where will be placed your graph.png image
}
