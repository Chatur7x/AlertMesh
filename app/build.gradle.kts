plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

android {
    namespace = "com.alertmesh"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.alertmesh"
        minSdk = 24
        targetSdk = 35
        versionCode = 5
        versionName = "4.1.0-rc1"
    }

    val releaseStorePath = System.getenv("ALERTMESH_RELEASE_STORE_FILE")
    val releaseStorePassword = System.getenv("ALERTMESH_RELEASE_STORE_PASSWORD")
    val releaseKeyAlias = System.getenv("ALERTMESH_RELEASE_KEY_ALIAS")
    val releaseKeyPassword = System.getenv("ALERTMESH_RELEASE_KEY_PASSWORD")
    val releaseSigningReady = listOf(releaseStorePath, releaseStorePassword, releaseKeyAlias, releaseKeyPassword)
        .all { !it.isNullOrBlank() }
    if (System.getenv("ALERTMESH_REQUIRE_SIGNED_RELEASE") == "1" && !releaseSigningReady) {
        error("Release signing variables are required for a signed release build")
    }
    signingConfigs {
        if (releaseSigningReady) {
            create("alertmeshRelease") {
                storeFile = file(releaseStorePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".devtest"
        }
        getByName("release") {
            isDebuggable = false
            if (releaseSigningReady) signingConfig = signingConfigs.getByName("alertmeshRelease")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":protocol")) {
        exclude(group = "com.google.crypto.tink", module = "tink")
    }

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.fragment:fragment:1.6.2")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("com.google.crypto.tink:tink-android:1.15.0")
    implementation("com.google.android.gms:play-services-nearby:19.3.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.7.3")
    implementation("androidx.core:core-ktx:1.15.0")
}
