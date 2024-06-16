import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

// Read local.properties
val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))

// Get the API key
val googleAiApiKey: String = localProperties.getProperty("googleAiApiKey")

android {
    namespace = "pt.ipt.DAMA"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "pt.ipt.DAMA"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "GOOGLE_AI_API_KEY", "\"$googleAiApiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.assets)
    implementation(libs.androidx.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.com.squareup.okhttp3.okhttp2)
    implementation(libs.logging.interceptor)
    //Glide
    implementation(libs.glide)
    implementation(libs.compiler)

    // google ai
    implementation(libs.googleAi)
    implementation(libs.couroutines)

    implementation(libs.core)
    implementation(libs.sceneform.ux)
}