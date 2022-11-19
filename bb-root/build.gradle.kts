plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

apply(plugin = "dagger.hilt.android.plugin")

android {
    namespace = "eu.darken.bb.common.root"
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
    }

    setupModuleBuildTypes()

    setupCompileOptions()

    setupKotlinOptions()

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
}
repositories {
    maven { setUrl("https://jitpack.io") }
}
dependencies {
    implementation(project(":bb-common"))

    addAndroidCore()
    addDI()
    addCoroutines()
    addSerialization()
    addIO()
    addTesting()

    implementation("com.jakewharton.timber:timber:+")

    implementation("com.github.d4rken.rxshell:core:v3.0.0")
    implementation("com.github.d4rken.rxshell:root:v3.0.0")
//
//    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.kotlin.coroutines}"
//    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:${versions.kotlin.coroutines}"
//    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-rx3:${versions.kotlin.coroutines}"
//    testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:${versions.kotlin.coroutines}"

}
