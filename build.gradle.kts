plugins {
    id("org.jetbrains.kotlin.android")
    id("com.android.application")
}

repositories {
    mavenCentral()
    google()
}

object Versions {
    const val dji_sdk = "5.7.0"
}


android {
    namespace = "com.sph.diagnostics.dji"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.sph.diagnostics.dji"
        minSdk = 25
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    androidTestImplementation("org.slf4j:slf4j-api:2.0.7")

    androidTestImplementation("org.assertj:assertj-core:3.24.2")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    androidTestImplementation("com.dji:dji-sdk-v5-aircraft:${Versions.dji_sdk}")

    androidTestCompileOnly("com.dji:dji-sdk-v5-aircraft-provided:${Versions.dji_sdk}")
    androidTestRuntimeOnly("com.dji:dji-sdk-v5-networkImp:${Versions.dji_sdk}")
}