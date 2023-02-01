plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    // This should be in-sync with the Gradle version exposed by `Versions.kt`
    implementation("com.android.tools.build:gradle:7.4.0")

    // This should be in-sync with the Kotlin version exposed by `Versions.kt`
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")

    implementation("com.squareup:javapoet:1.13.0")

    implementation(kotlin("script-runtime"))
}
