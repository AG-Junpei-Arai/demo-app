import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}
println("✅ build.gradle.kts の設定を読み込みました。")

android {
    namespace = "com.example.testapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.testapplication"
        minSdk = 21
        targetSdk = 35
        val (versionName, versionCode) = getGitVersionInfo()
        this.versionName = versionName
        this.versionCode = versionCode

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // "is" プレフィックスが付きます
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

/**
 * Gitタグからバージョン情報を取得
 */
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


/**
 * カスタムクリーンタスクで不要なファイルを掃除
 */

// buildディレクトリ以外の不要なファイルを掃除するカスタムクリーンタスク
tasks.register<Delete>("extendedClean") {
    description = "Deletes files and directories outside of the build directory."
    group = "build"

    // `generateDummyFiles`が生成したファイルやディレクトリを削除対象に指定
    delete(
        "some-library-cache",
        "specific-config.xml",
        "logs"
    )
}

tasks.named("preBuild") {
    dependsOn(tasks.named("extendedClean"))

    doFirst {
        println("➡️ Running 'extendedClean' automatically before the build process starts...")
    }
}

/**
 * 画像リソースの最適化
 */

// 1. 画像を最適化するカスタムタスクを定義
tasks.register<Exec>("optimizeImages") {
    group = "build"
    description = "Optimizes PNG and JPEG resources."

    // ■ 修正点: commandLineに関数を直接、個別に渡す
    commandLine(
        "bash",
        "-c",
        // シェルスクリプトは1つの文字列として渡す
        """
        find src/main/res -type f \( -name "*.png" -o -name "*.jpg" -o -name "*.jpeg" \) | while read file; do
            if [[ ${'$'}file == *.png ]]; then
                echo "Optimizing PNG: ${'$'}file"
                pngquant --force --skip-if-larger --output "${'$'}file" -- "${'$'}file"
            elif [[ ${'$'}file == *.jpg || ${'$'}file == *.jpeg ]]; then
                echo "Optimizing JPEG: ${'$'}file"
                guetzli --quality 85 --overwrite "${'$'}file"
            fi
        done
        """
    )
}

// 2. ビルドプロセスへの組み込み
tasks.whenTaskAdded {
    if (name.contains("merge") && name.contains("Resources")) {
        dependsOn("optimizeImages")
    }
}