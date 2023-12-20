plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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

    packagingOptions {
        doNotStrip.add("*/*/libconstants.so")
        doNotStrip.add("*/*/libdji_innertools.so")
        doNotStrip.add("*/*/libdjibase.so")
        doNotStrip.add("*/*/libDJICSDKCommon.so")
        doNotStrip.add("*/*/libDJIFlySafeCore-CSDK.so")
        doNotStrip.add("*/*/libdjifs_jni-CSDK.so")
        doNotStrip.add("*/*/libDJIRegister.so")
        doNotStrip.add("*/*/libdjisdk_jni.so")
        doNotStrip.add("*/*/libDJIUpgradeCore.so")
        doNotStrip.add("*/*/libDJIUpgradeJNI.so")
        doNotStrip.add("*/*/libDJIWaypointV2Core-CSDK.so")
        doNotStrip.add("*/*/libdjiwpv2-CSDK.so")
        doNotStrip.add("*/*/libffmpeg.so")
        doNotStrip.add("*/*/libFlightRecordEngine.so")
        doNotStrip.add("*/*/libvideo-framing.so")
        doNotStrip.add("*/*/libwaes.so")
        doNotStrip.add("*/*/libagora-rtsa-sdk.so")
        doNotStrip.add("*/*/libc++.so")
        doNotStrip.add("*/*/libc++_shared.so")
        doNotStrip.add("*/*/libmrtc_28181.so")
        doNotStrip.add("*/*/libmrtc_agora.so")
        doNotStrip.add("*/*/libmrtc_core.so")
        doNotStrip.add("*/*/libmrtc_core_jni.so")
        doNotStrip.add("*/*/libmrtc_data.so")
        doNotStrip.add("*/*/libmrtc_log.so")
        doNotStrip.add("*/*/libmrtc_onvif.so")
        doNotStrip.add("*/*/libmrtc_rtmp.so")
        doNotStrip.add("*/*/libmrtc_rtsp.so")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    androidTestImplementation("com.dji:dji-sdk-v5-aircraft:${Versions.dji_sdk}")

    androidTestCompileOnly("com.dji:dji-sdk-v5-aircraft-provided:${Versions.dji_sdk}")
    androidTestRuntimeOnly("com.dji:dji-sdk-v5-networkImp:${Versions.dji_sdk}")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.dji:dji-sdk-v5-aircraft:${Versions.dji_sdk}")
    compileOnly("com.dji:dji-sdk-v5-aircraft-provided:${Versions.dji_sdk}")
    runtimeOnly("com.dji:dji-sdk-v5-networkImp:${Versions.dji_sdk}")
}