plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("androidx.navigation.safeargs.kotlin") // ⬅️ Ganti alias dengan langsung ID ini
    id("kotlin-kapt")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "id.creatodidak.kp3k"
    compileSdk = 35

    defaultConfig {
        applicationId = "id.creatodidak.kp3k"
        minSdk = 30
        targetSdk = 35
        versionCode = 12
        versionName = "Alpha-1.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    signingConfigs {
        create("release") {
            storeFile = file(project.property("RELEASE_STORE_FILE") as String)
            storePassword = project.property("RELEASE_STORE_PASSWORD") as String
            keyAlias = project.property("RELEASE_KEY_ALIAS") as String
            keyPassword = project.property("RELEASE_KEY_PASSWORD") as String
        }
    }

    buildTypes {

        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
             buildConfigField("String", "BASE_URL", "\"https://server.resldk.cloud/\"")
             buildConfigField("String", "AGORA_ID", "\"6141104f89c84e5f89063dffef241e71\"")
        }
        debug {
             buildConfigField("String", "BASE_URL", "\"http://36.93.138.110:3011/\"")
            signingConfig = signingConfigs.getByName("release")
//            buildConfigField("String", "BASE_URL", "\"https://server.resldk.cloud/\"")
             buildConfigField("String", "AGORA_ID", "\"6141104f89c84e5f89063dffef241e71\"")
        }
    }

    // ⚠️ Tambahkan blok packaging di sini
    packaging {
        resources {
            // Kecualikan semua META-INF duplikat
            excludes.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/DEPENDENCIES.txt")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/NOTICE.txt")
            excludes.add("META-INF/io.netty.versions.properties")
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
        compose = true
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation (libs.glide)
    implementation (libs.core)
    implementation (libs.permissionsdispatcher)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.androidx.legacy.support.v4)
    kapt(libs.permissionsdispatcher.processor)
    implementation (libs.play.services.location)
    implementation (libs.material.v180)
    implementation (libs.gson)
    implementation (libs.play.services.maps)
    //noinspection UseTomlInstead
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")
    //noinspection UseTomlInstead
    implementation ("com.google.maps.android:android-maps-utils:2.2.0")
    //noinspection UseTomlInstead
    implementation ("io.agora.rtc:full-sdk:4.5.2")
    //noinspection UseTomlInstead
    implementation("io.socket:socket.io-client:2.0.1") {
        exclude(group = "org.json", module = "json")
    }
    //noinspection UseTomlInstead
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    //noinspection UseTomlInstead
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    //noinspection UseTomlInstead
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    //noinspection UseTomlInstead
    implementation ("androidx.camera:camera-core:1.4.2")
    //noinspection UseTomlInstead
    implementation ("androidx.camera:camera-camera2:1.4.2")
    //noinspection UseTomlInstead
    implementation ("androidx.camera:camera-lifecycle:1.4.2")
    //noinspection UseTomlInstead
    implementation ("androidx.camera:camera-view:1.4.2")
    //noinspection UseTomlInstead
    implementation ("androidx.camera:camera-extensions:1.4.2")
    //noinspection UseTomlInstead
    implementation ("com.google.guava:guava:33.3.1-android")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-video:1.4.2")
    //noinspection UseTomlInstead
    implementation ("androidx.room:room-runtime:2.7.1")
    //noinspection UseTomlInstead
    implementation ("androidx.room:room-ktx:2.7.1")
    //noinspection UseTomlInstead,KaptUsageInsteadOfKsp
    kapt ("androidx.room:room-compiler:2.7.1")
    //noinspection UseTomlInstead
    implementation ("androidx.biometric:biometric:1.1.0")
    //noinspection UseTomlInstead
    implementation ("com.github.yalantis:ucrop:2.2.8")
    //noinspection UseTomlInstead
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")
    //noinspection UseTomlInstead
    implementation ("com.google.maps.android:android-maps-utils:2.2.3")

    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}