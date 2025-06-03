plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.googleServices)
    kotlin("kapt")
}

android {
    namespace = "ipvc.tp.devhive"
    compileSdk = 35

    defaultConfig {
        applicationId = "ipvc.tp.devhive"
        minSdk = 24
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Defaults
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Room
    kapt(libs.androidx.room.compiler) {
        exclude(group = "com.intellij", module = "annotations")
    }
    implementation(libs.androidx.room.ktx) {
        exclude(group = "com.intellij", module = "annotations")
    }

    // Koin
    // implementation(libs.koin.android)
   //  implementation(libs.koin.androidx.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Jetpack, LiveData, ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Coil & Glide
    implementation(libs.coil)
    implementation(libs.glide)

    // Gson
    implementation(libs.gson)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // CircleImageView, SwipeRefreshLayout & ViewPager2
    implementation(libs.circleimageview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.swiperefreshlayout)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

kapt {
    correctErrorTypes = true
    useBuildCache = false
}