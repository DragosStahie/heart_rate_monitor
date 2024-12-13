plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinParcelize)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.dragosstahie.heartratemonitor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dragosstahie.heartratemonitor"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".develop"
            isMinifyEnabled = false
            isDebuggable = true

            buildConfigField("String", "VERSION_NAME_SUFFIX", "\".d\"")
            buildConfigField("Boolean", "DEBUG_SETTINGS_ALLOWED", "true")
        }

        release {
            applicationIdSuffix = ".release"
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")

            buildConfigField("String", "VERSION_NAME_SUFFIX", "\".r\"")
            buildConfigField("Boolean", "DEBUG_SETTINGS_ALLOWED", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.materialCompose)
    implementation(libs.composeUiToolingPreview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.composeUiTooling)

    implementation(libs.koinCompose)

    ksp(libs.roomCompiler)
    implementation(libs.roomKtx)

    implementation(libs.moshi)
    implementation(libs.moshiAdapters)
    ksp(libs.moshiCodeGen)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.ui.test.manifest)
}