plugins {
    id("com.android.application")
}

val releaseStoreFile = providers.environmentVariable("RELEASE_KEYSTORE_FILE").orNull
val releaseStorePassword = providers.environmentVariable("RELEASE_STORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").orNull
val hasReleaseSigning = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

android {
    namespace = "io.github.kelebao.colorosclockseconds"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.kelebao.colorosclockseconds"
        minSdk = 29
        targetSdk = 35
        versionCode = 220
        versionName = "1.2.0-test.20261030-0138"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(requireNotNull(releaseStoreFile))
                storePassword = requireNotNull(releaseStorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            // 调试期用生产 key 签名，便于直接覆盖安�?v1.0.4 �?release 包抓日志（无需卸载）�?
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    // 启用 BuildConfig 生成（AGP 8.x 默认关闭），�?release 构建�?BuildConfig.DEBUG 门控排查日志�?
    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // 改用 legacy Xposed API（与 betterclock 一致，已在 ColorOS 15 真机验证不崩溃）
    compileOnly("de.robv.android.xposed:api:82")
    // 单元测试（纯函数测试，不依赖 Android 运行时）
    testImplementation("junit:junit:4.13.2")
}