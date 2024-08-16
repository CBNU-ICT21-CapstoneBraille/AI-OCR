plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.test"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.test"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    dependencies {
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.9.0")
        implementation("androidx.activity:activity:1.7.2")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

        // CameraX Libraries
        implementation("androidx.camera:camera-core:1.4.0-rc01")
        implementation("androidx.camera:camera-camera2:1.4.0-rc01")
        implementation("androidx.camera:camera-lifecycle:1.4.0-rc01")
        implementation("androidx.camera:camera-video:1.4.0-rc01")
        implementation("androidx.camera:camera-view:1.4.0-rc01")
        implementation("androidx.camera:camera-mlkit-vision:1.4.0-rc01")
        implementation("androidx.camera:camera-extensions:1.4.0-rc01")

        // ML Kit 라이브러리 추가
        implementation("com.google.mlkit:text-recognition:16.0.1")
        implementation ("com.google.mlkit:text-recognition-korean:16.0.1")
        implementation("com.google.mlkit:vision:16.3.0")
    }

}
