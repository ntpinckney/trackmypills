import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.trackmypills"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.trackmypills"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.2"

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

    applicationVariants.all {
        outputs.all {
            val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val date = SimpleDateFormat("yyyyMMdd_HHmm").format(Date())
            val appName = "TrackMyPills"
            outputImpl.outputFileName = "${appName}_v${versionName}_${date}.apk"
        }
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
    implementation(libs.room.runtime)
    implementation(libs.firebase.messaging)
    implementation(libs.work.runtime)
    annotationProcessor(libs.room.complier)

    // JUnit Dependencies
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    androidTestImplementation(libs.junit.v115)
    androidTestImplementation(libs.espresso.core.v351)

    // Mockito Dependencies
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.junit.jupiter)

    implementation(libs.core.ktx)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
