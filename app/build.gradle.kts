import com.android.build.api.dsl.VariantDimension
import com.android.build.api.variant.BuildConfigField
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.compose)
    jacoco
}

// The two BSC networks the app knows. Each build type picks one; every build also carries both
// (TESTNET_* / MAINNET_*) so debug and staging can switch at runtime (config/ChainConfig.kt).
data class ChainSpec(
    val id: Long,
    val name: String,
    val rpcUrl: String,
    val ezptToken: String,
    val usdtToken: String,
    val explorerTxUrl: String,
)

val bscMainnet = ChainSpec(
    id = 56L,
    name = "BSC Mainnet",
    rpcUrl = "https://bsc-dataseed.bnbchain.org",
    ezptToken = "0x50387a24253150e808875f3cac8111546623ba64",
    usdtToken = "0x55d398326f99059fF775485246999027B3197955",
    explorerTxUrl = "https://bscscan.com/tx/",
)

val bscTestnet = ChainSpec(
    id = 97L,
    name = "BSC Testnet",
    rpcUrl = "https://data-seed-prebsc-1-s1.bnbchain.org:8545",
    ezptToken = "0xCe135658a2E4db9F503B7922E2429c37d9d21DFc",
    usdtToken = "0x337610d27c682E347C9cD60BD4b3b107C9d34dDd",
    explorerTxUrl = "https://testnet.bscscan.com/tx/",
)

/** The chain a build signs for and reads from (BuildConfig.CHAIN_ID, RPC_URL, ...). */
fun VariantDimension.chain(spec: ChainSpec) {
    buildConfigField("long", "CHAIN_ID", "${spec.id}L")
    buildConfigField("String", "CHAIN_NAME", "\"${spec.name}\"")
    buildConfigField("String", "RPC_URL", "\"${spec.rpcUrl}\"")
    buildConfigField("String", "EZPT_TOKEN_CONTRACT", "\"${spec.ezptToken}\"")
    buildConfigField("String", "USDT_TOKEN_CONTRACT", "\"${spec.usdtToken}\"")
    buildConfigField("String", "EXPLORER_TX_URL", "\"${spec.explorerTxUrl}\"")
}

/** One of the chains the in-app switch can choose, as <prefix>_CHAIN_ID, <prefix>_RPC_URL, ... */
fun VariantDimension.knownChain(prefix: String, spec: ChainSpec) {
    buildConfigField("long", "${prefix}_CHAIN_ID", "${spec.id}L")
    buildConfigField("String", "${prefix}_CHAIN_NAME", "\"${spec.name}\"")
    buildConfigField("String", "${prefix}_RPC_URL", "\"${spec.rpcUrl}\"")
    buildConfigField("String", "${prefix}_EZPT_TOKEN_CONTRACT", "\"${spec.ezptToken}\"")
    buildConfigField("String", "${prefix}_USDT_TOKEN_CONTRACT", "\"${spec.usdtToken}\"")
    buildConfigField("String", "${prefix}_EXPLORER_TX_URL", "\"${spec.explorerTxUrl}\"")
}

// Firebase is optional per build type. The google-services plugin reads the first file that exists
// of src/<buildType>/google-services.json and app/google-services.json, and fails when that file has
// no client for the variant's application id. Variants without one build with Firebase switched off.
fun googleServicesFileFor(buildType: String): File? =
    listOf("src/$buildType/google-services.json", "google-services.json")
        .map { project.file(it) }
        .firstOrNull { it.exists() }

fun hasFirebaseClient(buildType: String, applicationId: String): Boolean {
    val json = googleServicesFileFor(buildType)?.readText() ?: return false
    return Regex("\"package_name\"\\s*:\\s*\"${Regex.escape(applicationId)}\"").containsMatchIn(json)
}

fun gradleValue(name: String): String? =
    project.findProperty(name)?.toString()?.takeIf { it.isNotBlank() }

// Staging: the staging server, BSC testnet unless -PstagingChain=mainnet.
val stagingApiBaseUrl = gradleValue("stagingApiBaseUrl") ?: "http://18.142.108.53/api/v1/"
val stagingChainName = (gradleValue("stagingChain") ?: "testnet").lowercase()
val stagingChain = when (stagingChainName) {
    "testnet" -> bscTestnet
    "mainnet" -> bscMainnet
    else -> throw GradleException("-PstagingChain must be testnet or mainnet, got '$stagingChainName'")
}
val productionApiBaseUrl = gradleValue("productionApiBaseUrl") ?: "https://api.ezipaycoin.com/api/v1/"
check(productionApiBaseUrl.startsWith("https://")) { "productionApiBaseUrl must be an https:// URL" }

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

        // Production endpoints and contracts (BSC mainnet). Debug, staging and sandbox override
        // them; release always ships these.
        buildConfigField("boolean", "SANDBOX", "false")
        buildConfigField("String", "API_BASE_URL", "\"https://api.ezipaycoin.com/api/v1/\"")
        chain(bscMainnet)
        knownChain("TESTNET", bscTestnet)
        knownChain("MAINNET", bscMainnet)
        // The in-app network switch (Profile) exists only where a build type turns this on.
        buildConfigField("boolean", "CHAIN_SWITCHABLE", "false")
        // "Withdraw (On-chain)" asks the payments vendor for a merchant-funded payout with no debit
        // of the user's balance, so it stays hidden in every build until the backend debits a
        // verified balance first.
        buildConfigField("boolean", "FEATURE_ONCHAIN_WITHDRAW", "false")
    }

    // Release signing comes from gradle properties (~/.gradle/gradle.properties) or the environment:
    // EZIPAY_RELEASE_STORE_FILE, EZIPAY_RELEASE_STORE_PASSWORD, EZIPAY_RELEASE_KEY_ALIAS,
    // EZIPAY_RELEASE_KEY_PASSWORD. When any is missing the release build is left unsigned.
    fun signingValue(name: String): String? =
        (project.findProperty(name) as String?)?.takeIf { it.isNotBlank() }
            ?: System.getenv(name)?.takeIf { it.isNotBlank() }

    val releaseStoreFile = signingValue("EZIPAY_RELEASE_STORE_FILE")
    val releaseStorePassword = signingValue("EZIPAY_RELEASE_STORE_PASSWORD")
    val releaseKeyAlias = signingValue("EZIPAY_RELEASE_KEY_ALIAS")
    val releaseKeyPassword = signingValue("EZIPAY_RELEASE_KEY_PASSWORD")
    val hasReleaseSigning = listOf(
        releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword
    ).all { it != null }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("release")?.let { signingConfig = it }
            // -PproductionApiBaseUrl=https://... moves release to another HTTPS host; the default is production.
            buildConfigField("String", "API_BASE_URL", "\"$productionApiBaseUrl\"")
        }

        debug {
            enableUnitTestCoverage = true
            // The .NET API running on this machine (port 5009; 5000 is taken by the macOS AirPlay receiver).
            // 10.0.2.2 is how the Android emulator reaches the host. For a USB device run
            // `adb reverse tcp:5009 tcp:5009` and build with -PapiBaseUrl=http://127.0.0.1:5009/api/v1/.
            val apiBaseUrl = project.findProperty("apiBaseUrl")?.toString() ?: "http://10.0.2.2:5009/api/v1/"
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")

            // Debug builds default to BSC testnet (97) with the testnet contracts.
            chain(bscTestnet)
            buildConfigField("boolean", "CHAIN_SWITCHABLE", "true")

            // A separate applicationId lets debug and release coexist on one device. It needs a
            // matching Firebase app, so it applies only once google-services.json knows the id.
            // The plugin prefers src/debug/google-services.json (separate non-production Firebase
            // project) over app/google-services.json.
            val debugId = "com.app.ezipaycoin.debug"
            if (hasFirebaseClient("debug", debugId)) {
                applicationIdSuffix = ".debug"
                versionNameSuffix = "-debug"
            } else {
                logger.warn("no google-services.json has a client for $debugId; debug keeps the release applicationId. Register the app in Firebase to enable the .debug suffix.")
            }
        }

        // Staging: a release-like build (minified, not debuggable) for the team, installed next
        // to production. Its server and chain come from -PstagingApiBaseUrl and -PstagingChain.
        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            matchingFallbacks += listOf("release")
            isDebuggable = false
            buildConfigField("String", "API_BASE_URL", "\"$stagingApiBaseUrl\"")
            chain(stagingChain)
            buildConfigField("boolean", "CHAIN_SWITCHABLE", "true")
            // The release key when it is configured; otherwise the debug key, so the APK still installs.
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
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
                buildConfigField("long", "CHAIN_ID", "${chainId}L")
                buildConfigField("String", "CHAIN_NAME", "\"Local chain $chainId\"")
                buildConfigField("String", "EZPT_TOKEN_CONTRACT", "\"${value("token.ezpt")}\"")
                buildConfigField("String", "USDT_TOKEN_CONTRACT", "\"${value("token.usdt")}\"")
                // The local chain is the only one the sandbox server knows.
                buildConfigField("boolean", "CHAIN_SWITCHABLE", "false")
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
    testOptions {
        unitTests {
            // android.util.Log and friends return defaults instead of throwing in JVM tests.
            isReturnDefaultValues = true
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Firebase per variant: BuildConfig.FIREBASE_ENABLED, and no google-services processing where the
// variant has no Firebase client. Release always keeps the plugin's check, so a production build
// without its Firebase config still fails instead of shipping without push.
val firebaseOffVariants = mutableMapOf<String, String>()
androidComponents {
    onVariants { variant ->
        val buildType = variant.buildType ?: variant.name
        val applicationId = variant.applicationId.get()
        val enabled = buildType == "release" || hasFirebaseClient(buildType, applicationId)
        variant.buildConfigFields.put(
            "FIREBASE_ENABLED",
            BuildConfigField("boolean", enabled.toString(), "False when no google-services.json lists this application id")
        )
        if (!enabled) firebaseOffVariants[variant.name] = applicationId
    }
}
tasks.configureEach {
    if (firebaseOffVariants.keys.any { name == "process${it.replaceFirstChar(Char::uppercaseChar)}GoogleServices" }) {
        enabled = false
    }
}

fun hasReleaseSigningConfig(): Boolean = android.signingConfigs.findByName("release") != null

// One line per build that actually runs a staging or Firebase-less variant, not on every build.
gradle.taskGraph.whenReady {
    val taskNames = allTasks.filter { it.project == project }.map { it.name }
    fun runs(variant: String) = taskNames.any { it.contains(variant.replaceFirstChar(Char::uppercaseChar)) }
    if (runs("staging")) {
        val other = if (stagingChain == bscTestnet) "mainnet" else "testnet"
        logger.lifecycle(
            "Staging build targets ${stagingChain.name.uppercase()} (${stagingChain.id}) at $stagingApiBaseUrl - pass -PstagingChain=$other to switch"
        )
        if (!hasReleaseSigningConfig()) {
            logger.lifecycle("Staging is signed with the debug key (no EZIPAY_RELEASE_* values): fine for team installs, not for Play uploads.")
        }
    }
    firebaseOffVariants.forEach { (variant, applicationId) ->
        if (runs(variant)) {
            logger.lifecycle("Firebase is off in $variant: no google-services.json lists $applicationId (push and social sign-in disabled).")
        }
    }
}

// Guard against shipping the wrong chain: release is mainnet only, debug is never mainnet by
// default, staging is testnet or mainnet. Only debug and staging may switch chain at runtime.
// (The sandbox build type has its own check where it is defined.)
afterEvaluate {
    fun fieldOf(buildType: String, field: String): String? =
        (android.buildTypes.getByName(buildType).buildConfigFields[field] ?: android.defaultConfig.buildConfigFields[field])?.value?.toString()
    fun chainOf(name: String): Long? = fieldOf(name, "CHAIN_ID")?.removeSuffix("L")?.toLong()
    check(chainOf("release") == 56L) { "release must use chain 56, found ${chainOf("release")}" }
    check(chainOf("debug") != 56L) { "debug must not use chain 56" }
    check(chainOf("staging") in setOf(56L, 97L)) { "staging must use chain 97 or 56, found ${chainOf("staging")}" }
    check(fieldOf("release", "CHAIN_SWITCHABLE") == "false") { "release must not switch chain at runtime" }
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
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

// Coverage for debug unit tests: XML (for scripts/test-report.sh) and HTML (for people).
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Coverage report for the debug unit tests."
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val excluded = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*ComposableSingletons*.*", "**/*\$\$inlined*.*", "**/*Kt\$*lambda*.*"
    )
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) { exclude(excluded) }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) { include("outputs/unit_test_code_coverage/debugUnitTest/*.exec", "jacoco/testDebugUnitTest.exec") }
    )
}
