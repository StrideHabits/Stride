import java.io.FileInputStream
import java.util.Properties

/**
 * Function to retrieve a secret value from a property file.
 *
 * **Note:** This should be used only for the `local.properties` repository
 * and these values should never be shared or committed to a version control
 * system in any form.
 *
 * @param property The name of the property to retrieve.
 * @param filename The name of the file to read from (should not be changed)
 *
 * @return The value of the requested property or an empty string if the
 *         file or property could not be found.
 *
 * @author MP
 */
fun getLocalSecret(
    property: String,
    filename: String = "local.properties"
): String {
    val properties = Properties()
    val propertiesFile = rootProject.file(filename)
    if (propertiesFile.exists()) {
        properties.load(FileInputStream(propertiesFile))
    } else {
        println("Local property file not found.")
    }

    return properties.getProperty(property) ?: ""
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

android {
    namespace = "com.mpieterse.stride"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mpieterse.stride"
        minSdk = 34
        targetSdk = 36
        versionCode = 2
        versionName = "2025m10a"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // --- Environment Variables
        
        buildConfigField(
            type = "String",
            name = "GOOGLE_SERVER_CLIENT_ID",
            value = "\"${
                getLocalSecret("GOOGLE_SERVER_CLIENT_ID")
            }\""
        )

        buildConfigField(
            type = "String",
            name = "API_BASE_URL",
            value = "\"https://summitapi.onrender.com/\""
        )

        val clientId = project.findProperty("GOOGLE_SERVER_CLIENT_ID") as? String
            ?: System.getenv("GOOGLE_SERVER_CLIENT_ID")
            ?: getLocalSecret("GOOGLE_SERVER_CLIENT_ID")

        if (clientId.isBlank()) {
            logger.warn("GOOGLE_SERVER_CLIENT_ID is not set. Build may fail in release mode.")
        }
    }

    afterEvaluate {
        val isReleaseBuild = gradle.startParameter.taskNames.any {
            it.contains("release", ignoreCase = true)
        }

        if (isReleaseBuild) {
            val clientId = project.findProperty("GOOGLE_SERVER_CLIENT_ID") as? String
                ?: System.getenv("GOOGLE_SERVER_CLIENT_ID")
                ?: getLocalSecret("GOOGLE_SERVER_CLIENT_ID")

            if (clientId.isBlank()) {
                throw GradleException(
                    """
                    | GOOGLE_SERVER_CLIENT_ID is required for release builds.
                    | Provide it via:
                    |   1. Command line: -PGOOGLE_SERVER_CLIENT_ID=your-id
                    |   2. Environment: export GOOGLE_SERVER_CLIENT_ID=your-id
                    |   3. local.properties: GOOGLE_SERVER_CLIENT_ID=your-id
                    """.trimMargin()
                )
            }
        }
    }
    
    signingConfigs {
        create("release") {
            storeFile = file("release.jks")
            storePassword = "mattpieterse"
            keyAlias = "release"
            keyPassword = "mattpieterse"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { 
        jvmTarget = "17" 
    }
    
    kotlin { 
        jvmToolchain(17) 
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.runtime.saveable)
    implementation(libs.googleid)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.room.ktx)
    ksp(libs.hilt.android.compiler)
    implementation("com.google.dagger:hilt-android:2.57.1")
    ksp("com.google.dagger:hilt-android-compiler:2.57.1")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.ui.text.google.fonts)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.activity.ktx)
    implementation("androidx.browser:browser:1.9.0")
    implementation("androidx.room:room-runtime:2.8.3")
    ksp("androidx.room:room-compiler:2.8.3")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:compiler:4.16.0")

    testImplementation(libs.junit)
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.logging.interceptor)
}