plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val signingPath = providers.environmentVariable("SPEAKIVRA_KEYSTORE_PATH").orNull

android {
    namespace = "com.nghialcvn.speakivra"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.nghialcvn.speakivra"
        minSdk = 26
        targetSdk = 35
        versionCode = 9
        versionName = "0.1.8"
    }
    signingConfigs {
        if (signingPath != null) create("stableRelease") {
            storeFile = file(signingPath)
            storePassword = providers.environmentVariable("SPEAKIVRA_STORE_PASSWORD").get()
            keyAlias = providers.environmentVariable("SPEAKIVRA_KEY_ALIAS").get()
            keyPassword = providers.environmentVariable("SPEAKIVRA_KEY_PASSWORD").get()
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            if (signingPath != null) signingConfig = signingConfigs.getByName("stableRelease")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}

// Also guard local assembleRelease, which otherwise emits an unsigned APK.
val requireReleaseSigning = tasks.register("requireReleaseSigning") {
    doLast {
        check(signingPath != null) { "Configure the fixed Speakivra release signing secrets first." }
    }
}
tasks.configureEach {
    if (name == "preReleaseBuild") dependsOn(requireReleaseSigning)
}
