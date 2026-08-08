plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

import java.util.Properties

android {
    namespace = "com.iskcon.bhagavaddarshan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.iskcon.bhagavaddarshan"
        minSdk = 24
        targetSdk = 35
        versionCode = 9
        versionName = "1.6.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProps = Properties()
        val localFile = rootProject.file("local.properties")
        if (localFile.exists()) {
            localFile.inputStream().use { localProps.load(it) }
        }
        fun prop(name: String): String =
            (localProps.getProperty(name) ?: "").trim().replace("\"", "\\\"")

        buildConfigField("String", "RAZORPAY_KEY_ID", "\"${prop("razorpay.keyId")}\"")
        buildConfigField("String", "RAZORPAY_KEY_SECRET", "\"${prop("razorpay.keySecret")}\"")
        buildConfigField("String", "UPI_VPA", "\"${prop("upi.vpa")}\"")
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

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
