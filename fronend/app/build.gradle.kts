plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.pintegrador.servicios"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pintegrador.servicios"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // → Para usar imágenes vectoriales (svg) en todos los niveles
        vectorDrawables {
            useSupportLibrary = true
        }
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

    // (Opcional) Si quieres evitar findViewById en tus Activities
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        // Bien con Java 11 (Retrofit/OkHttp/Glide están OK)
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Jetpack (desde tu Version Catalog)
    implementation(libs.appcompat)
    implementation(libs.material)          // Material Components 1.12.x
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Red de datos
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Imágenes
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}
