import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localProps = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
}
val pexelsApiKey = localProps.getProperty("PEXELS_API_KEY", "").replace("\"", "")

android {
    namespace = "com.roamio"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.roamio"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "PEXELS_API_KEY", "\"$pexelsApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
        allWarningsAsErrors = true
    }
    lint {
        lintConfig = file("lint.xml")
        warningsAsErrors = true
        abortOnError = true
        disable += "AndroidGradlePluginVersion"
        disable += "GradleDependency"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    val composeBom = platform(libs.androidx.compose.bom)
    //compose
    implementation(libs.androidx.activity.compose)
    implementation(composeBom)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    //koin dependency injection
    implementation(libs.koin.android)
    //compose navigation
    implementation(libs.androidx.navigation.compose.android)
    // Timber for logging
    implementation(libs.timber)
    //Modules declaration
    implementation(project(":core"))
    implementation(project(":feature-onboarding"))
    implementation(project(":feature-home"))
    implementation(libs.koin.androidx.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}