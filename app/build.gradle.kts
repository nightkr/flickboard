import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization") version "2.0.21"
}

val keystorePropertiesFile = rootProject.file("../flickboard.keystore.properties")
val keystoreProperties = keystorePropertiesFile
    .takeIf { it.exists() }
    ?.let { propFile ->
        Properties().also { props ->
            FileInputStream(propFile).use(props::load)
        }
    }

android {
    signingConfigs {
        if (keystoreProperties != null) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        } else {
            logger.warn("$keystorePropertiesFile not found, no release signing config loaded")
        }
    }
    namespace = "se.nullable.flickboard"
    compileSdk = 35

    defaultConfig {
        applicationId = "se.nullable.flickboard"
        minSdk = 28
        targetSdk = 35
        versionCode = 56
        versionName = "0.2.0"
        base.archivesName = "flickboard-v${versionName}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (keystoreProperties != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        create("unsignedRelease") {
            initWith(getByName("release"))
            applicationIdSuffix = ".unsigned"
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    flavorDimensions += "purpose"
    productFlavors {
        create("plain") {
            dimension = "purpose"
        }
        create("beta") {
            dimension = "purpose"
        }
        create("screengrab") {
            dimension = "purpose"
            applicationIdSuffix = ".screengrab"
            base.archivesName = "app"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }
    kotlinOptions {
        jvmTarget = "19"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // Should be included transitively, but doesn't actually seem to render emojis
    // without a direct dependency.
    // Emojis still not rendering properly? Try gradle clean and then rebuilding.
    implementation("androidx.emoji2:emoji2:1.5.0")
    implementation("androidx.emoji2:emoji2-emojipicker:1.5.0")
    implementation("androidx.emoji2:emoji2-bundled:1.5.0")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    // Runtime library for kotlin serialization plugin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("tools.fastlane:screengrab:2.1.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}