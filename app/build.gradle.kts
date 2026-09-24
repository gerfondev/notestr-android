import java.security.MessageDigest

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "fr.decentralia.notestr"
    compileSdk = 36
    testBuildType = providers.gradleProperty("androidTestBuildType").getOrElse("debug")

    defaultConfig {
        applicationId = "fr.decentralia.notestr"
        minSdk = 26
        targetSdk = 36
        versionCode = 20
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Preserve the certificate used by existing installations, while
            // shipping a non-debuggable release without debug-only dependencies.
            signingConfig = signingConfigs.getByName("debug")
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


    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging { jniLibs.useLegacyPackaging = true }
}

dependencies {
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.fragment:fragment:1.8.9")
    val composeBom = platform("androidx.compose:compose-bom:2025.08.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(files(rootProject.file("vendor/nostr-sdk/nostr-sdk-0.45.1-notestr.1.aar")))
    implementation(files(rootProject.file("vendor/jna/jna-5.19.1-notestr.1.aar")))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("androidx.appcompat:appcompat:1.8.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

// Fail before packaging if the locally rebuilt SDK no longer matches its recorded artifact.
val verifyNostrSdk by tasks.registering {
    val sdk = rootProject.file("vendor/nostr-sdk/nostr-sdk-0.45.1-notestr.1.aar")
    val sums = rootProject.file("vendor/nostr-sdk/SHA256SUMS")
    inputs.files(sdk, sums)
    doLast {
        val expected = sums.readText().trim().substringBefore(" ")
        val actual = MessageDigest.getInstance("SHA-256").digest(sdk.readBytes())
            .joinToString("") { "%02x".format(it) }
        check(actual == expected) { "Le SDK natif ne correspond pas à son empreinte vérifiée." }
    }
}
tasks.named("preBuild") { dependsOn(verifyNostrSdk) }

val verifyJna by tasks.registering {
    val library = rootProject.file("vendor/jna/jna-5.19.1-notestr.1.aar")
    val sums = rootProject.file("vendor/jna/SHA256SUMS")
    inputs.files(library, sums)
    doLast {
        val expected = sums.readText().trim().substringBefore(" ")
        val actual = MessageDigest.getInstance("SHA-256").digest(library.readBytes())
            .joinToString("") { "%02x".format(it) }
        check(actual == expected) { "JNA ne correspond pas à son empreinte vérifiée." }
    }
}
tasks.named("preBuild") { dependsOn(verifyJna) }
