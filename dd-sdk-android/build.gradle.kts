/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import com.datadog.gradle.config.AndroidConfig
import com.datadog.gradle.config.BuildConfigPropertiesKeys
import com.datadog.gradle.config.GradlePropertiesKeys
import com.datadog.gradle.config.dependencyUpdateConfig
import com.datadog.gradle.config.javadocConfig
import com.datadog.gradle.config.junitConfig
import com.datadog.gradle.config.kotlinConfig
import com.datadog.gradle.config.publishingConfig
import com.datadog.gradle.config.setLibraryVersion

plugins {
    // Build
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")

    // Publish
    `maven-publish`
    signing
    id("org.jetbrains.dokka")

    // Analysis tools
    id("com.github.ben-manes.versions")

    // Tests
    id("de.mobilej.unmock")

    // Internal Generation
    id("thirdPartyLicences")
    id("apiSurface")
    id("transitiveDependencies")
}

/**
 * Checks whether logcat logs should be enabled when building the release version of the library.
 * @return true if logcat logs should be enabled
 */
fun isLogEnabledInRelease(): String {
    return project.findProperty(GradlePropertiesKeys.FORCE_ENABLE_LOGCAT) as? String ?: "false"
}

android {
    compileSdk = AndroidConfig.TARGET_SDK
    buildToolsVersion = AndroidConfig.BUILD_TOOLS_VERSION

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK
        targetSdk = AndroidConfig.TARGET_SDK

        setLibraryVersion()

        consumerProguardFiles("consumer-rules.pro")
    }

    namespace = "com.datadog.android"

    sourceSets.named("main") {
        java.srcDir("src/main/kotlin")
    }
    sourceSets.named("test") {
        java.srcDir("src/test/kotlin")
    }
    sourceSets.named("androidTest") {
        java.srcDir("src/androidTest/kotlin")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.apply {
            isReturnDefaultValues = true
        }
    }

    buildTypes {
        getByName("release") {
            buildConfigField(
                "Boolean",
                BuildConfigPropertiesKeys.LOGCAT_ENABLED,
                isLogEnabledInRelease()
            )
        }

        getByName("debug") {
            buildConfigField(
                "Boolean",
                BuildConfigPropertiesKeys.LOGCAT_ENABLED,
                "true"
            )
        }
    }

    libraryVariants.configureEach {
        addJavaSourceFoldersToModel(
            layout.buildDirectory
                .dir("generated/ksp/$name/kotlin").get().asFile
        )
    }

    packagingOptions {
        resources {
            excludes += listOf(
                "META-INF/jvm.kotlin_module",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md"
            )
        }
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        checkReleaseBuilds = false
        checkGeneratedSources = true
        ignoreTestSources = true
    }
}

dependencies {
    implementation(libs.kotlin)

    // Network
    implementation(libs.okHttp)
    implementation(libs.gson)
    implementation(libs.kronosNTP)

    // Android Instrumentation
    implementation(libs.androidXAnnotation)
    implementation(libs.androidXWorkManager)

    // Generate NoOp implementations
    ksp(project(":tools:noopfactory"))

    // Testing
    testImplementation(project(":tools:unit"))
    testImplementation(libs.bundles.jUnit5)
    testImplementation(libs.bundles.testTools)
    testImplementation(libs.okHttpMock)
    testImplementation(libs.bundles.openTracing)
    unmock(libs.robolectric)

    // Static Analysis
    // TODO MTG-12 detekt(project(":tools:detekt"))
    // TODO MTG-12 detekt(libs.detektCli)
}

unMock {
    keep("android.os.BaseBundle")
    keep("android.os.Bundle")
    keep("android.os.Parcel")
    keepStartingWith("com.android.internal.util.")
    keepStartingWith("android.util.")
    keep("android.content.ComponentName")
    keep("android.os.Looper")
    keep("android.os.MessageQueue")
    keep("android.os.SystemProperties")
    keep("android.view.Choreographer")
    keep("android.view.DisplayEventReceiver")
    keepStartingWith("org.json")
}

kotlinConfig()
junitConfig()
javadocConfig()
dependencyUpdateConfig()
publishingConfig("Datadog monitoring library for Android applications.")
