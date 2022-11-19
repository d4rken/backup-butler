plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
}
apply(plugin = "dagger.hilt.android.plugin")
apply(plugin = "androidx.navigation.safeargs.kotlin")
apply(plugin = "com.bugsnag.android.gradle")

android {
    val packageName = ProjectConfig.packageName

    namespace = ProjectConfig.packageName

    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        applicationId = packageName

        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk

        versionCode = ProjectConfig.Version.code
        versionName = ProjectConfig.Version.name

        testInstrumentationRunner = "eu.darken.octi.HiltTestRunner"

        buildConfigField("String", "PACKAGENAME", "\"${ProjectConfig.packageName}\"")
        buildConfigField("String", "GITSHA", "\"${lastCommitHash()}\"")
        buildConfigField("String", "BUILDTIME", "\"${buildTime()}\"")
        buildConfigField("String", "VERSION_CODE", "\"${ProjectConfig.Version.code}\"")
        buildConfigField("String", "VERSION_NAME", "\"${ProjectConfig.Version.name}\"")

        manifestPlaceholders["bugsnagApiKey"] = getBugSnagApiKey(
            File(System.getProperty("user.home"), ".appconfig/${packageName}/bugsnag.properties")
        ) ?: "fake"
    }

    signingConfigs {
        val basePath = File(System.getProperty("user.home"), ".appconfig/${packageName}")
        create("releaseFoss") {
            setupCredentials(File(basePath, "signing-foss.properties"))
        }
        create("releaseGplay") {
            setupCredentials(File(basePath, "signing-gplay.properties"))
        }
    }

    flavorDimensions.add("version")
    productFlavors {
        create("foss") {
            dimension = "version"
            signingConfig = signingConfigs["releaseFoss"]
        }
        create("gplay") {
            dimension = "version"
            signingConfig = signingConfigs["releaseGplay"]
        }
    }

    buildTypes {
        val customProguardRules = fileTree(File(projectDir, "proguard")) {
            include("*.pro")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFiles(*customProguardRules.toList().toTypedArray())
            proguardFiles("proguard-rules-debug.pro")
        }
        create("beta") {
            lint {
                abortOnError = true
                fatal.add("StopShip")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFiles(*customProguardRules.toList().toTypedArray())
        }
        release {
            lint {
                abortOnError = true
                fatal.add("StopShip")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFiles(*customProguardRules.toList().toTypedArray())
        }
    }

    buildOutputs.all {
        val variantOutputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
        val variantName: String = variantOutputImpl.name

        if (listOf("release", "beta").any { variantName.toLowerCase().contains(it) }) {
            val outputFileName = packageName +
                "-v${defaultConfig.versionName}-${defaultConfig.versionCode}" +
                "-${variantName.toUpperCase()}-${lastCommitHash()}.apk"

            variantOutputImpl.outputFileName = outputFileName
        }
    }

    buildFeatures {
        viewBinding = true
    }

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

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${Versions.Desugar.core}")

    api(project(":bb-common"))
    api(project(":bb-root"))

    addDI()
    addCoroutines()
    addSerialization()
    addIO()

    addAndroidCore()
    addAndroidUI()
    addWorkerManager()

    addTesting()

    addNavigation()

//    implementation("org.jetbrains.kotlin:kotlin-reflect:${versions.kotlin.core}")
//
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.kotlin.coroutines}")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${versions.kotlin.coroutines}")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx3:${versions.kotlin.coroutines}")
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${versions.kotlin.coroutines}")

    implementation("androidx.core:core-ktx:1.7.0")

    // Support libs
    implementation("androidx.annotation:annotation:1.3.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0-beta01")
    implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:2.4.0-beta01")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.4.0-beta01")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.3.1")
    implementation("androidx.lifecycle:lifecycle-process:2.3.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")

    implementation("androidx.activity:activity-ktx:1.4.0-alpha02")
    implementation("androidx.fragment:fragment-ktx:1.4.0-alpha09")
    implementation("androidx.viewpager2:viewpager2:1.1.0-beta01")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.core:core-splashscreen:1.0.0-beta02")

    implementation("androidx.fragment:fragment-ktx:1.5.0-alpha05")

//    implementation("androidx.navigation:navigation-fragment-ktx:${versions.androidx.navigation}")
//    implementation("androidx.navigation:navigation-ui-ktx:${versions.androidx.navigation}")

    implementation("com.airbnb.android:lottie:3.5.0")

    val work_version = "2.7.0"
    implementation("androidx.work:work-runtime:${work_version}")
    testImplementation("androidx.work:work-testing:${work_version}")

    // Flow
    val flowbinding_version = "1.2.0"

    implementation("io.github.reactivecircus.flowbinding:flowbinding-android:${flowbinding_version}")

    implementation("io.github.reactivecircus.flowbinding:flowbinding-activity:${flowbinding_version}")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-appcompat:${flowbinding_version}")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-core:${flowbinding_version}")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-drawerlayout:${flowbinding_version}")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-lifecycle:${flowbinding_version}")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-navigation:${flowbinding_version}")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-preference:${flowbinding_version}")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-recyclerview:${flowbinding_version}")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-swiperefreshlayout:${flowbinding_version}")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-viewpager:${flowbinding_version}")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-viewpager2:${flowbinding_version}")

    implementation("io.github.reactivecircus.flowbinding:flowbinding-material:${flowbinding_version}")

    // IO
    implementation("com.squareup.okio:okio:3.0.0")

    // Serialization
    implementation("com.squareup.moshi:moshi:1.13.0")
    implementation("com.squareup.moshi:moshi-adapters:1.13.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")

    implementation("org.apache.commons:commons-compress:1.21")

    // ROOM
    val room_version = "2.4.0-alpha04"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    implementation("androidx.room:room-rxjava3:$room_version")
    testImplementation("androidx.room:room-testing:$room_version")

    //Dagger
//    implementation("com.google.dagger:dagger:${versions.dagger}")
//    implementation("com.google.dagger:dagger-android:${versions.dagger}")
//
//    kapt ("com.google.dagger:dagger-compiler:${versions.dagger}")
//    kapt ("com.google.dagger:dagger-android-processor:${versions.dagger}")
//
//    implementation("com.google.dagger:hilt-android:${versions.dagger}")
//    kapt ("com.google.dagger:hilt-android-compiler:${versions.dagger}")
//
//    testImplementation("com.google.dagger:hilt-android-testing:${versions.dagger}")
//    kaptTest ("com.google.dagger:hilt-android-compiler:${versions.dagger}")
//
//    androidTestImplementation("com.google.dagger:hilt-android-testing:${versions.dagger}")
//    kaptAndroidTest ("com.google.dagger:hilt-android-compiler:${versions.dagger}")

    implementation("androidx.hilt:hilt-work:1.0.0")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.work:work-runtime-ktx:2.7.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.9.0")
    kapt("com.github.bumptech.glide:compiler:4.9.0")

    // Root
    implementation("eu.chainfire:libsuperuser:+")

    // Rx
    implementation("io.reactivex.rxjava3:rxjava:+")
    implementation("io.reactivex.rxjava3:rxandroid:+")
    implementation("io.reactivex.rxjava3:rxkotlin:+")
    implementation("com.jakewharton.rx3:replaying-share:3.0.0")
    implementation("com.jakewharton.rx3:replaying-share-kotlin:3.0.0")

    implementation("com.github.d4rken.rxshell:core:v3.0.0")
    implementation("com.github.d4rken.rxshell:root:v3.0.0")

    // Debug
    implementation("com.jakewharton.timber:timber:+")
//    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.0-beta-3")
//    implementation("com.uber.rxdogtag2:rxdogtag:2.1.0-SNAPSHOT")
    implementation("com.bugsnag:bugsnag-android:5.9.2")

    /**
     * Unit testing
     */
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.1")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.7.1")

    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("io.kotest:kotest-runner-junit5:4.6.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.6.2")
    testImplementation("io.kotest:kotest-property-jvm:4.6.2")

    testImplementation("org.awaitility:awaitility:4.0.2")
    testImplementation("org.awaitility:awaitility-kotlin:4.0.2")

    testImplementation("androidx.test.ext:junit:1.1.3")

    // A specific version just used within tests to format output when we don"t want or can mock JSONObject
    //noinspection GradleDependency
    testImplementation("org.json:json:20140107")

    testImplementation("org.robolectric:robolectric:4.3.1")
    // Robolectric requires jUnit4
    testImplementation("androidx.test.ext:junit:1.1.1")
    testImplementation("junit:junit:4.13")
    testImplementation("org.robolectric:robolectric:4.3.1")


    /**
     * Instrumentation testing
     */
    androidTestImplementation("org.mockito:mockito-core:2.26.0")
    androidTestImplementation("org.mockito:mockito-android:2.23.0")
    androidTestImplementation("com.github.tmurakami:dexopener:1.0.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.2.0")
    androidTestImplementation("androidx.test.espresso.idling:idling-concurrent:3.2.0")
}