import java.util.Properties

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

        // Production endpoints and contracts (BSC mainnet). Only the sandbox build
        // type below overrides them; release always ships these.
        buildConfigField("boolean", "SANDBOX", "false")
        buildConfigField("String", "API_BASE_URL", "\"https://api.ezipaycoin.com/api/v1/\"")
        buildConfigField("String", "RPC_URL", "\"https://bsc-dataseed.bnbchain.org\"")
        buildConfigField("String", "IRIPASH_BASE_URL", "\"https://apiusdt.iripash.com/api/Payment/\"")
        buildConfigField("long", "CHAIN_ID", "56L")
        buildConfigField("String", "EZPT_TOKEN_CONTRACT", "\"0x50387a24253150e808875f3cac8111546623ba64\"")
        buildConfigField("String", "USDT_TOKEN_CONTRACT", "\"0x55d398326f99059fF775485246999027B3197955\"")
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

            // Debug builds never touch mainnet: BSC testnet (97) with the testnet contracts.
            buildConfigField("String", "RPC_URL", "\"https://data-seed-prebsc-1-s1.bnbchain.org:8545\"")
            buildConfigField("long", "CHAIN_ID", "97L")
            buildConfigField("String", "EZPT_TOKEN_CONTRACT", "\"0xCe135658a2E4db9F503B7922E2429c37d9d21DFc\"")
            buildConfigField("String", "USDT_TOKEN_CONTRACT", "\"0x337610d27c682E347C9cD60BD4b3b107C9d34dDd\"")

            // A separate applicationId lets debug and release coexist on one device. It needs a
            // matching Firebase app, so it applies only once google-services.json knows the id.
            val debugId = "com.app.ezipaycoin.debug"
            // The plugin prefers src/debug/google-services.json (separate non-production Firebase
            // project) over app/google-services.json.
            val knowsDebugId = listOf("src/debug/google-services.json", "google-services.json")
                .map { file(it) }
                .any { it.exists() && it.readText().contains(debugId) }
            if (knowsDebugId) {
                applicationIdSuffix = ".debug"
                versionNameSuffix = "-debug"
            } else {
                logger.warn("no google-services.json has a client for $debugId; debug keeps the release applicationId. Register the app in Firebase to enable the .debug suffix.")
            }
        }

        // Local sandbox: a debug build wired to the zero-cost sandbox in ezipay-coin-platform
        // (local API, chain 31337, test tokens). It exists only while sandbox.properties does,
        // so deleting that file detaches it; its values never reach a release build.
        val sandboxFile = file("sandbox.properties")
        if (sandboxFile.exists()) {
            val sandbox = Properties().apply { sandboxFile.inputStream().use { load(it) } }
            val host = project.findProperty("sandboxHost")?.toString() ?: sandbox.getProperty("host")
            fun value(key: String): String =
                requireNotNull(sandbox.getProperty(key)) { "sandbox.properties has no $key" }.replace("{host}", host)
            val chainId = value("chain.id")
            check(chainId != "56") { "sandbox.properties must not point at BSC mainnet" }
            create("sandbox") {
                initWith(getByName("debug"))
                matchingFallbacks += "debug"
                buildConfigField("boolean", "SANDBOX", "true")
                buildConfigField("String", "API_BASE_URL", "\"${value("api.baseUrl")}\"")
                buildConfigField("String", "RPC_URL", "\"${value("rpc.url")}\"")
                buildConfigField("String", "IRIPASH_BASE_URL", "\"${value("iripash.baseUrl")}\"")
                buildConfigField("long", "CHAIN_ID", "${chainId}L")
                buildConfigField("String", "EZPT_TOKEN_CONTRACT", "\"${value("token.ezpt")}\"")
                buildConfigField("String", "USDT_TOKEN_CONTRACT", "\"${value("token.usdt")}\"")
            }
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

// Guard against shipping the wrong chain: release is mainnet only, debug is never mainnet.
// (The sandbox build type has its own check where it is defined.)
afterEvaluate {
    fun chainOf(name: String): Long? =
        (android.buildTypes.getByName(name).buildConfigFields["CHAIN_ID"] ?: android.defaultConfig.buildConfigFields["CHAIN_ID"])?.value?.toString()?.removeSuffix("L")?.toLong()
    check(chainOf("release") == 56L) { "release must use chain 56, found ${chainOf("release")}" }
    check(chainOf("debug") != 56L) { "debug must not use chain 56" }
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
    implementation(libs.androidx.compose.foundation)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

}
