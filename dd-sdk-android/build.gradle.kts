/*
 * Unless explicitly stated otherwise all pomFilesList in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-2019 Datadog, Inc.
 */

import com.datadog.gradle.Dependencies
import com.datadog.gradle.androidTestImplementation
import com.datadog.gradle.config.AndroidConfig
import com.datadog.gradle.config.BuildConfigPropertiesKeys
import com.datadog.gradle.config.GradlePropertiesKeys
import com.datadog.gradle.config.bintrayConfig
import com.datadog.gradle.config.dependencyUpdateConfig
import com.datadog.gradle.config.detektConfig
import com.datadog.gradle.config.jacocoConfig
import com.datadog.gradle.config.javadocConfig
import com.datadog.gradle.config.junitConfig
import com.datadog.gradle.config.kotlinConfig
import com.datadog.gradle.config.ktLintConfig
import com.datadog.gradle.config.publishingConfig
import com.datadog.gradle.testImplementation

plugins {
    id("com.android.library")
    id("androidx.benchmark")
    kotlin("android")
    kotlin("android.extensions")
    `maven-publish`
    id("com.github.ben-manes.versions")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("thirdPartyLicences")
    id("org.jetbrains.dokka")
    id("com.jfrog.bintray")
    jacoco
}

val isLogEnabledInRelease: String
    get() {
        return if (project.hasProperty(GradlePropertiesKeys.FORCE_ENABLE_LOGCAT)) {
            project.property(GradlePropertiesKeys.FORCE_ENABLE_LOGCAT) as String
        } else {
            "false"
        }
    }
val isLogEnabledInDebug: String
    get() {
        return "true"
    }

android {
    compileSdkVersion(AndroidConfig.TARGET_SDK)
    buildToolsVersion(AndroidConfig.BUILD_TOOLS_VERSION)

    defaultConfig {
        minSdkVersion(AndroidConfig.MIN_SDK)
        targetSdkVersion(AndroidConfig.TARGET_SDK)
        versionCode = AndroidConfig.VERSION.code
        versionName = AndroidConfig.VERSION.name

        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
        testInstrumentationRunnerArgument("androidx.benchmark.suppressErrors",
            "EMULATOR,UNLOCKED")
        testInstrumentationRunnerArgument("androidx.benchmark.suppressErrors",
            "EMULATOR,DEBUGGABLE")
        testInstrumentationRunnerArgument("androidx.benchmark.output.enable", "true")
    }

    sourceSets.named("main") {
        java.srcDir("src/main/kotlin")
    }
    sourceSets.named("test") {
        java.srcDir("src/test/kotlin")
    }
    sourceSets.named("androidTest") {
        java.srcDir("src/androidTest/kotlin")
    }

    // TODO when using Android Plugin 3.6.+
    // enableAdditionalTestOutput=true
    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    buildTypes {
        getByName("release") {
            buildConfigField("Boolean",
                    BuildConfigPropertiesKeys.LOGCAT_ENABLED,
                    isLogEnabledInRelease)
        }

        getByName("debug") {
            buildConfigField("Boolean",
                    BuildConfigPropertiesKeys.LOGCAT_ENABLED,
                    isLogEnabledInDebug)
        }
    }
}

dependencies {
    implementation(Dependencies.Libraries.Gson)
    implementation(Dependencies.Libraries.Kotlin)
    implementation(Dependencies.Libraries.OkHttp)

    testImplementation(project(":tools:unit"))
    testImplementation(Dependencies.Libraries.JUnit5)
    testImplementation(Dependencies.Libraries.TestTools)
    testImplementation(Dependencies.Libraries.OkHttpMock)

    androidTestImplementation(Dependencies.Libraries.AndroidTestTools)
    androidTestImplementation(Dependencies.Libraries.JetpackBenchmark)
    androidTestImplementation(Dependencies.Libraries.OkHttpMock)

    detekt(project(":tools:detekt"))
    detekt(Dependencies.Libraries.DetektCli)
}

kotlinConfig()
detektConfig()
ktLintConfig()
junitConfig()
jacocoConfig()
javadocConfig()
dependencyUpdateConfig()
publishingConfig("${rootDir.canonicalPath}/repo")
bintrayConfig()
