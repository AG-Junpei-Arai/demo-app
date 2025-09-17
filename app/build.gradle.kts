import java.io.ByteArrayOutputStream

// Gitタグからバージョン情報を取得するヘルパー関数
fun getGitVersionInfo(): Pair<String, Int> {
    try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", "describe", "--tags", "--abbrev=0")
            standardOutput = stdout
        }
        val fullTag = stdout.toString().trim()

        // ★正規表現を "1.6.1 -52" の形式に合うように変更★
        val regex = """(\d+\.\d+\.\d+)\s*-(\d+)""".toRegex()
        val matchResult = regex.find(fullTag)

        if (matchResult != null) {
            val (name, code) = matchResult.destructured
            println("Git tag found: $fullTag -> versionName: $name, versionCode: $code")
            return Pair(name, code.toInt())
        }
    } catch (e: Exception) {
        println("Git tag not found or format is incorrect. Using default version.")
    }
    // デフォルト値を返す
    println("Using default version: 0.1.0-SNAPSHOT, versionCode: 1")
    return Pair("0.1.0-SNAPSHOT", 1)
}

val (versionName, versionCode) = getGitVersionInfo()

println("✅ build.gradle.kts の設定を読み込みました。")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.testapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.testapplication"
        minSdk = 21
        targetSdk = 35
        // 取得したバージョン情報を設定
        this.versionName = versionName
        this.versionCode = versionCode

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // "is" プレフィックスが付きます
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    }
}

dependencies {
    // バージョンカタログを使用しているため、dependenciesブロックの変更は不要です
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}