plugins {
    alias(libs.plugins.android.library)

    alias(libs.plugins.kover)
}

android {
    namespace = "dev.daika.davy.core.common"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
