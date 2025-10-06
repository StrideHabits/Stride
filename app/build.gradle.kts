import java.io.FileInputStream
import java.util.Properties

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

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.mpieterse.stride"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "V0"

        // --- Tooling Configuration

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // --- Environment Variables

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
            property: String, filename: String = "local.properties"
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

        buildConfigField(
            type = "String",
            name = "API_BASE_URL",
            value = "\"https://summitapi.onrender.com/\""
        )

        buildConfigField(
            type = "String",
            name = "GOOGLE_SERVER_CLIENT_ID",
            value = "\"${
                getLocalSecret("GOOGLE_SERVER_CLIENT_ID")
            }\""
        )
    }

    afterEvaluate {
        val isEnvironmentProduction = gradle.startParameter.taskNames.any {
            it.contains("release", ignoreCase = true)
        }

        if (isEnvironmentProduction) {
            val clientId = (project.findProperty("GOOGLE_SERVER_CLIENT_ID") as? String).orEmpty()
            if (clientId.isBlank()) throw GradleException(
                "Essential property is missing from build: (use -P GOOGLE_SERVER_CLIENT_ID=...)"
            )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
        viewBinding = true
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
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.ui.text.google.fonts)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.androidx.material.icons.extended)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.logging.interceptor)
}