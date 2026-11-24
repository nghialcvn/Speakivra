plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val signingPath = providers.environmentVariable("SPEAKIVRA_KEYSTORE_PATH").orNull

android {
    namespace = "com.nghialcvn.speakivra"
    compileSdk = 35
    val approvedVersion = "1.0.1"
    require(Regex("""[0-9]+\.[0-9]\.[0-9]""").matches(approvedVersion)) {
        "Speakivra version must roll 1.0.9 -> 1.1.0, never 1.0.10"
    }
    defaultConfig {
        applicationId = "com.nghialcvn.speakivra"
        minSdk = 26
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 21
        versionName = approvedVersion
    }
    testBuildType = "release"
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
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
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
