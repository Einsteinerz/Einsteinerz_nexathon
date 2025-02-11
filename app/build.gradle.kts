import java.util.Properties  // Added import to fix unresolved reference "util"

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

val secretsFile = rootProject.file("secrets.properties")
val secrets = Properties()  // Corrected usage with explicit import
if (secretsFile.exists()) {
    secrets.load(secretsFile.inputStream())
} else {
    secrets.setProperty("OPENAI_API_KEY", System.getenv("OPENAI_API_KEY") ?: "")
}

android {
    namespace = "com.example.test_max"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.test_max"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "OPENAI_API_KEY", "\"${secrets.getProperty("OPENAI_API_KEY", "")}\"")
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

    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module",
                "META-INF/versions/9/module-info.class",
                "org/apache/logging/log4j/core/impl/Log4jContextFactory.class",
                "org/apache/logging/log4j/core/impl/Log4jProvider.class"
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Material Design and AppCompat
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Activity and Fragment
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Apache POI for Excel handling
    implementation("org.apache.poi:poi:5.0.0")
    implementation("org.apache.poi:poi-ooxml:5.0.0")
    implementation("org.apache.xmlbeans:xmlbeans:4.0.0")
    implementation("org.slf4j:slf4j-nop:1.7.36")

    // OpenAI API Client
    implementation("com.aallam.openai:openai-client:3.6.3")
    implementation("io.ktor:ktor-client-android:2.3.7")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Permissions handling
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
}
