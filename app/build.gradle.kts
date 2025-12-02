plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.tem2.karirku"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tem2.karirku"
        minSdk = 21
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

    // ✅ Google AI - Gemini 1.5 Flash (VERSI BENAR)
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
//    implementation("com.google.ai.client:generativeai")


    implementation("com.nex3z:flow-layout:1.3.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.google.guava:guava:31.0.1-android")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // Supabase Storage
    implementation("io.github.jan-tennert.supabase:storage-kt:2.3.1")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.3.1")

    // Ktor Client
    implementation("io.ktor:ktor-client-android:2.3.11")
    implementation("io.ktor:ktor-client-okhttp:2.3.11")

    // Circle Image View
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.google.android.flexbox:flexbox:3.0.0")
    // Supabase untuk versi terbaru
    implementation("io.github.jan-tennert.supabase:storage-kt:2.3.1")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.3.1")
    implementation("io.github.jan-tennert.supabase:supabase-kt:2.3.1")

    // Ktor Client (sudah ada)
    implementation("io.ktor:ktor-client-android:2.3.11")
    implementation("io.ktor:ktor-client-okhttp:2.3.11")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Glide (sudah ada)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
