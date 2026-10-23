plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")

    kotlin("plugin.serialization") version "2.3.20"

    id("jacoco-report-aggregation")
}

android {
    namespace = "dev.daika.davy"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "dev.daika.davy"
        minSdk = 26
        targetSdk = 36
        versionCode = 9
        versionName = "1.3.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt")
            )
        }
        debug {
            enableUnitTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":feature:mode-selection"))
    implementation(project(":feature:home"))
    implementation(project(":feature:details"))
    implementation(project(":feature:player"))
    implementation(project(":feature:search"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    ksp(libs.hilt.android.compiler)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.register<JacocoReport>("aggregatedJacocoReport") {
    group = "Verification"
    description = "Generates aggregated JaCoCo coverage report for all modules"

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*_MembersInjector.*",
        "**/Dagger*.*",
        "**/Hilt_*.*",
        "**/*_Factory.*",
        "**/*_Factory$*.*",
        "**/*_Provide*Factory*.*",
        "**/*ComposableSingletons*.*",
        "**/*Companion*.*",
        "hilt_aggregated_deps/**",
        "dagger/hilt/**"
    )

    val targetProjects = listOf(
        project,
        project(":core:common"),
        project(":core:domain"),
        project(":core:data"),
        project(":feature:home"),
        project(":feature:details"),
        project(":feature:player"),
        project(":feature:search"),
    )

    dependsOn(targetProjects.map { "${it.path}:testDebugUnitTest" })

    val classTrees = targetProjects.map { p ->
        fileTree(p.layout.buildDirectory) {
            include(
                "tmp/kotlin-classes/debug/dev/daika/davy/**",
                "intermediates/javac/debug/**/classes/dev/daika/davy/**"
            )
            exclude(fileFilter)
        }
    }
    classDirectories.setFrom(files(classTrees))

    val sources = targetProjects.flatMap { p ->
        listOf(
            p.file("src/main/java"),
            p.file("src/main/kotlin")
        )
    }.filter { it.exists() }
    sourceDirectories.setFrom(files(sources))

    val execFiles = targetProjects.map { p ->
        fileTree(p.layout.buildDirectory) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec"
            )
        }
    }
    executionData.setFrom(files(execFiles))
}