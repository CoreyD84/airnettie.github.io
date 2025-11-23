plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")       // ✅ Only in app module
    id("com.google.firebase.crashlytics")      // ✅ Safe to apply here
}

android {
    namespace = "com.airnettie.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.airnettie.mobile"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // ✅ Fill these in with your keystore details
            storeFile = file("momma-keystore.jks")
            storePassword = "Allen6516!"
            keyAlias = "mommaKey"
            keyPassword = "Allen6516!"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            // Debug builds don’t need signing, but you can add if required
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}

dependencies {
    // ✅ Kotlin and Material
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
    implementation("com.google.android.material:material:1.12.0")

    // ✅ Jetpack Compose (BOM-aligned)
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.core:core-ktx:1.13.1")

    // ✅ Firebase (BOM-aligned)
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // ✅ CameraX
    implementation("androidx.camera:camera-core:1.3.3")
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")

    // ✅ ML Kit
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // ✅ Biometric
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // ✅ Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // ✅ ZXing
    implementation("com.google.zxing:core:3.5.1")

    // ✅ Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.04.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}