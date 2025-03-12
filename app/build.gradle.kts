plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "fr.fork_chan"
    compileSdk = 35

    defaultConfig {
        applicationId = "fr.fork_chan"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.database)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)


    implementation (libs.coil.compose)

    // Jetpack Compose
    implementation (libs.ui) // Use the latest stable version
    implementation (libs.androidx.foundation)
    implementation (libs.material3)
    implementation (libs.androidx.activity.compose.v182)
    implementation (libs.androidx.lifecycle.viewmodel.compose)

   
    implementation (libs.google.firebase.firestore.ktx)
    implementation (libs.google.firebase.auth.ktx)
    implementation (libs.google.firebase.storage.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation ("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3") // For HTTP requests to Imgur
    implementation ("io.coil-kt:coil-compose:2.0.0")   // For loading images in Jetpack Compose (optional, use Glide if preferred)

    implementation ("com.google.firebase:firebase-auth-ktx:22.1.2")
    implementation ("com.google.firebase:firebase-firestore-ktx:24.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("io.coil-kt:coil-compose:2.0.0")

}