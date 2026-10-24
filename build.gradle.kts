// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false

    id("com.google.devtools.ksp") version "2.3.10" apply false
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
    kotlin("plugin.serialization") version "2.3.20" apply false

    alias(libs.plugins.kover)
}

dependencies {
    kover(project(":core:common"))
    kover(project(":core:domain"))
    kover(project(":core:data"))

    kover(project(":feature:home"))
    kover(project(":feature:details"))
    kover(project(":feature:player"))
    kover(project(":feature:search"))
}

kover {
    reports {
        filters {
            excludes {
                annotatedBy(
                    "*Composable",
                    "*Preview"
                )

                classes(
                    "hilt_aggregated_deps.*",
                    "*.*_Factory*",
                    "*.*_MembersInjector*",
                    "*.*NetworkModule*",
                    "*HiltModule*",

                    "*.data.api.*",

                    "*.*Destination*",
                    "*.*Destination$*",

                    "*.*ModifierUtils*",
                    "*.*ScreenKt*"
                )

                packages(
                    "*.ui"
                )
            }
        }
    }
}