// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.GRADLE}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.STDLIB}")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
        classpath("com.google.dagger:hilt-android-gradle-plugin:${Versions.Google.HILT}")
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
