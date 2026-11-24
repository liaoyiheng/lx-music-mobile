plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("io.github.takahirom.roborazzi")
}

val appVersionName: String = (project.findProperty("APP_VERSION_NAME") as String?) ?: "2.0.0-liquid"
val appVersionCode: Int = ((project.findProperty("APP_VERSION_CODE") as String?)?.toIntOrNull()) ?: 20002

android {
    namespace = "cn.toside.music.liquid"
    compileSdk = 35

    defaultConfig {
        applicationId = "cn.toside.music.liquid"
        minSdk = 24
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val storeFileProp = (project.findProperty("MYAPP_UPLOAD_STORE_FILE") as String?) ?: "debug.keystore"
            storeFile = file(storeFileProp)
            storePassword = (project.findProperty("MYAPP_UPLOAD_STORE_PASSWORD") as String?) ?: "android"
            keyAlias = (project.findProperty("MYAPP_UPLOAD_KEY_ALIAS") as String?) ?: "androiddebugkey"
            keyPassword = (project.findProperty("MYAPP_UPLOAD_KEY_PASSWORD") as String?) ?: "android"
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = true
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all { test ->
                test.systemProperty("robolectric.graphicsMode", "NATIVE")
                test.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
                test.systemProperty(
                    "roborazzi.record.filePathStrategy",
                    "relativePathFromRoborazziContextOutputDirectory",
                )
                test.jvmArgs("-Xmx2048m")
            }
        }
    }
}

roborazzi {
    outputDir.set(layout.buildDirectory.dir("outputs/roborazzi"))
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-service:2.8.7")

    // Jetpack Compose + Material Icons + Animation
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Coroutines & Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Jetpack Media3 (ExoPlayer + MediaSession + OkHttp DataSource)
    val media3Version = "1.5.0"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")
    implementation("androidx.media3:media3-datasource-okhttp:$media3Version")
    implementation("androidx.media3:media3-common:$media3Version")

    // Room Database + DataStore
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Networking & Image Loading & Color Extraction
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.palette:palette-ktx:1.0.0")

    // True Android 12+ Hardware RenderEffect Backdrop Blur (iOS 26 Liquid Glass)
    implementation("dev.chrisbanes.haze:haze:1.1.1")

    // QuickJS Runtime (100% compatible with LX Music SDK & Custom User API .js scripts)
    implementation("wang.harlon.quickjs:wrapper-android:2.4.0")

    // Unit Test & Roborazzi Compose Screenshot Rendering
    testImplementation(composeBom)
    testImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.48.0")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.48.0")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-junit-rule:1.48.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
