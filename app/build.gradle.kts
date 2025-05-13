plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp.plug)
}

android {
    namespace = "com.example.tictoe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tictoe"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.activity)

    // WebSocket library
    implementation("org.java-websocket:Java-WebSocket:1.5.3")

    // Gson for JSON serialization/deserialization
    implementation("com.google.code.gson:gson:2.10.1")

    // Room database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}