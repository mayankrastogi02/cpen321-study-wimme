import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    namespace = "com.cpen321.study_wimme"
    compileSdk = 35

    val file = rootProject.file("local.properties")
    val properties = Properties()
    properties.load(file.inputStream())

    defaultConfig {
        applicationId = "com.cpen321.study_wimme"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        val mapsAPIKey = properties.getProperty("MAPS_API_KEY") ?: ""
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsAPIKey\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsAPIKey

        val serverUrl = properties.getProperty("SERVER_URL") ?: ""
        buildConfigField("String", "SERVER_URL", "\"$serverUrl\"")

        val webClientId = properties.getProperty("WEB_CLIENT_ID") ?: ""
        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")

        val testGoogleId = properties.getProperty("TEST_GOOGLE_ID") ?: ""
        buildConfigField("String", "TEST_GOOGLE_ID", "\"$testGoogleId\"")

        val testUserId = properties.getProperty("TEST_USER_ID") ?: ""
        buildConfigField("String", "TEST_USER_ID", "\"$testUserId\"")

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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.androidx.espresso.intents)
    implementation(libs.androidx.rules)
//    implementation(libs.androidx.junit.ktx)
//    implementation(libs.androidx.rules)
//    implementation(libs.androidx.espresso.intents)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation(libs.firebase.messaging.ktx)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging:23.3.1")
    androidTestImplementation("com.squareup.okhttp3:okhttp:4.9.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("com.squareup.okhttp3:okhttp:4.9.3")
}