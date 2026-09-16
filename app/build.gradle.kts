plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

import java.util.Properties

android {
    namespace = "com.iskcon.bhagavaddarshan"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.iskcon.bhagavaddarshan"
        minSdk = 24
        targetSdk = 36
        versionCode = 42
        versionName = "1.2.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProps = Properties()
        val localFile = rootProject.file("local.properties")
        if (localFile.exists()) {
            localFile.inputStream().use { localProps.load(it) }
        }
        fun prop(name: String): String =
            (localProps.getProperty(name) ?: "").trim().replace("\"", "\\\"")

        // Public Checkout key only — never ship key secret / WhatsApp tokens in release.
        buildConfigField("String", "RAZORPAY_KEY_ID", "\"${prop("razorpay.keyId")}\"")
        buildConfigField("String", "UPI_VPA", "\"${prop("upi.vpa")}\"")
        // Production checkout uses real plan amounts. Debug can still override locally.
        buildConfigField("boolean", "TEST_PAYMENTS", "false")
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"${prop("api.baseUrl").ifBlank { "https://13-232-71-204.sslip.io" }}\""
        )

        // Empty in defaultConfig; debug may fill from local.properties below.
        buildConfigField("String", "RAZORPAY_KEY_SECRET", "\"\"")
        buildConfigField("String", "MNV_API_KEY", "\"\"")
        buildConfigField("String", "MNV_API_URL", "\"\"")
        buildConfigField("String", "MNV_CAMPAIGN_NAME", "\"\"")
        buildConfigField("String", "MNV_USERNAME", "\"\"")
        buildConfigField("String", "MNV_REGISTRATION_CAMPAIGN", "\"\"")
        buildConfigField("String", "MNV_DISPATCH_CAMPAIGN", "\"\"")
        buildConfigField("String", "WABA_API_KEY", "\"\"")
    }

    signingConfigs {
        getByName("debug")
        create("review") {
            storeFile = rootProject.file("review-keystore.jks")
            storePassword = "bdreview2026"
            keyAlias = "bdreview"
            keyPassword = "bdreview2026"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }
    buildTypes {
        debug {
            val localProps = Properties()
            val localFile = rootProject.file("local.properties")
            if (localFile.exists()) {
                localFile.inputStream().use { localProps.load(it) }
            }
            fun prop(name: String): String =
                (localProps.getProperty(name) ?: "").trim().replace("\"", "\\\"")

            // Local/dev: HTTP IP is fine until Lightsail HTTPS (443) is open.
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"${prop("api.baseUrl").ifBlank { "http://13.232.71.204" }}\""
            )
            buildConfigField("String", "RAZORPAY_KEY_SECRET", "\"${prop("razorpay.keySecret")}\"")
            buildConfigField("String", "MNV_API_KEY", "\"${prop("mnv.apiKey")}\"")
            buildConfigField("String", "MNV_API_URL", "\"${prop("mnv.apiUrl")}\"")
            buildConfigField("String", "MNV_CAMPAIGN_NAME", "\"${prop("mnv.campaignName")}\"")
            buildConfigField("String", "MNV_USERNAME", "\"${prop("mnv.username")}\"")
            buildConfigField(
                "String",
                "MNV_REGISTRATION_CAMPAIGN",
                "\"${prop("mnv.registrationCampaign")}\""
            )
            buildConfigField(
                "String",
                "MNV_DISPATCH_CAMPAIGN",
                "\"${prop("mnv.dispatchCampaign")}\""
            )
            buildConfigField("String", "WABA_API_KEY", "\"${prop("waba.apiKey")}\"")
            buildConfigField("boolean", "TEST_PAYMENTS", "true")
        }
        release {
            isMinifyEnabled = true
            // Shrink can strip density mipmaps / break OEM APK preview (Vivo folder closes).
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("review")
            // Embed native debug symbols in the AAB so Play can symbolicate crashes.
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            // Lightsail networking still blocks 443; HTTP :80 works until HTTPS is opened.
            buildConfigField("String", "API_BASE_URL", "\"http://13.232.71.204\"")
            // Secrets stay empty — payments/OTP go through the cloud API.
            buildConfigField("String", "RAZORPAY_KEY_SECRET", "\"\"")
            buildConfigField("String", "MNV_API_KEY", "\"\"")
            buildConfigField("String", "MNV_API_URL", "\"\"")
            buildConfigField("String", "MNV_CAMPAIGN_NAME", "\"\"")
            buildConfigField("String", "MNV_USERNAME", "\"\"")
            buildConfigField("String", "MNV_REGISTRATION_CAMPAIGN", "\"\"")
            buildConfigField("String", "MNV_DISPATCH_CAMPAIGN", "\"\"")
            buildConfigField("String", "WABA_API_KEY", "\"\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("com.razorpay:checkout:1.6.41")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
