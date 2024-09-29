plugins {
    //alias(libs.plugins.androidApplication)
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.puredrop"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.puredrop"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-analytics")
    val lottieVersion = "6.4.0"
    implementation("com.airbnb.android:lottie:$lottieVersion")
    implementation("com.etebarian:meow-bottom-navigation:1.2.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


}
