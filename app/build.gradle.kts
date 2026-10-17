plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.app.ezipaycoin"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.app.ezipaycoin"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        manifestPlaceholders["facebook_app_id"] = "833416492706197"

        buildConfigField("String", "API_BASE_URL", "\"https://api.ezipaycoin.com/api/v1/\"")
    }

     signingConfigs {
        create("release") {
            storeFile = file("../coin_keystore") // path to your keystore
            storePassword = "EzipayCoin"
            keyAlias = "key0"
            keyPassword = "EzipayCoin"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            // The .NET API running on this machine (port 5009; 5000 is taken by the macOS AirPlay receiver).
            // 10.0.2.2 is how the Android emulator reaches the host. For a USB device run
            // `adb reverse tcp:5009 tcp:5009` and build with -PapiBaseUrl=http://127.0.0.1:5009/api/v1/.
            val apiBaseUrl = project.findProperty("apiBaseUrl")?.toString() ?: "http://10.0.2.2:5009/api/v1/"
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation("com.trustwallet:wallet-core:4.3.13")
    implementation("androidx.webkit:webkit:1.10.0")
    implementation("androidx.compose.foundation:foundation:1.7.1")
    // For Icons
    implementation("androidx.compose.material:material-icons-core:1.6.7") // Or latest
    implementation("androidx.compose.material:material-icons-extended:1.6.7")
   // implementation(libs.androidx.navigation.runtime.android)
    implementation("androidx.activity:activity:1.8.1")
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.kizitonwose.calendar:compose:2.9.0")
    implementation(platform(libs.firebase.bom))

    implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.facebook.login)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.biometric)
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)
// ML Kit Barcode Scanner
    implementation(libs.barcode.scanning)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.androidx.ui.test.manifest)
}