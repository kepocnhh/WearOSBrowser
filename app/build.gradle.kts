import com.android.build.api.variant.ComponentIdentity
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import sp.gx.core.camelCase
import sp.gx.core.create
import sp.gx.core.getByName
import sp.gx.core.kebabCase

repositories {
    google()
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.mozilla.org/maven2")
}

plugins {
    kotlin("android")
    id("com.android.application")
    id("org.jetbrains.compose") version Version.compose
}

fun ComponentIdentity.getVersion(): String {
    if (productFlavors.isNotEmpty()) TODO()
    val versionName = android.defaultConfig.versionName ?: error("No version name!")
    check(versionName.isNotBlank())
    val versionCode = android.defaultConfig.versionCode ?: error("No version code!")
    check(versionCode > 0)
    check(name.isNotBlank())
    return when (buildType) {
        "debug" -> kebabCase(
            versionName,
            name,
            versionCode.toString(),
        )
        "release" -> kebabCase(
            versionName,
            versionCode.toString(),
        )
        else -> error("Build type \"${buildType}\" is not supported!")
    }
}

android {
    namespace = "org.kepocnhh.wosbrowser"
    compileSdk = Version.Android.compileSdk

    defaultConfig {
        applicationId = namespace
        minSdk = Version.Android.minSdk
        targetSdk = Version.Android.targetSdk
        versionCode = 1
        versionName = "0.0.$versionCode"
        manifestPlaceholders["appName"] = "@string/app_name"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".$name"
            versionNameSuffix = "-$name"
            isMinifyEnabled = false
            isShrinkResources = false
            manifestPlaceholders["buildType"] = name
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions.kotlinCompilerExtensionVersion = "1.5.14"
}

androidComponents.onVariants { variant ->
    val output = variant.outputs.single()
    check(output is com.android.build.api.variant.impl.VariantOutputImpl)
    output.outputFileName = "${kebabCase(rootProject.name, variant.getVersion())}.apk"
    afterEvaluate {
        tasks.getByName<JavaCompile>("compile", variant.name, "JavaWithJavac") {
            targetCompatibility = Version.jvmTarget
        }
        tasks.getByName<KotlinCompile>("compile", variant.name, "Kotlin") {
            kotlinOptions.jvmTarget = Version.jvmTarget
        }
        val checkManifestTask = tasks.create("checkManifest", variant.name) {
            dependsOn(camelCase("compile", variant.name, "Sources"))
            doLast {
                val file = "intermediates/merged_manifest/${variant.name}/AndroidManifest.xml"
                val manifest = groovy.xml.XmlParser().parse(layout.buildDirectory.file(file).get().asFile)
                val actual = manifest.getAt(groovy.namespace.QName("uses-permission")).map {
                    check(it is groovy.util.Node)
                    val attributes = it.attributes().mapKeys { (k, _) -> k.toString() }
                    val name = attributes["{http://schemas.android.com/apk/res/android}name"]
                    check(name is String && name.isNotEmpty())
                    name
                }
                val applicationId by variant.applicationId
                val expected = setOf(
                    "$applicationId.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
                    "android.permission.ACCESS_NETWORK_STATE",
                    "android.permission.INTERNET",
                )
                check(actual.sorted() == expected.sorted()) {
                    "Actual is:\n$actual\nbut expected is:\n$expected"
                }
            }
        }
        tasks.getByName(camelCase("assemble", variant.name)) {
            dependsOn(checkManifestTask)
        }
    }
}

dependencies {
    implementation(compose.foundation)
    implementation("androidx.appcompat:appcompat:1.6.1")
//    debugImplementation("androidx.compose.ui:ui-tooling:${Version.Android.compose}")
//    debugImplementation("androidx.compose.ui:ui-tooling-preview:${Version.Android.compose}")
//    debugImplementation("androidx.wear:wear-tooling-preview:1.0.0")
//    implementation("androidx.security:security-crypto:1.0.0")
//    implementation("com.github.kepocnhh:Logics:0.1.3-SNAPSHOT")
//    implementation("com.github.kepocnhh:Storages:0.4.2u-SNAPSHOT")
//    implementation("com.squareup.okhttp3:okhttp:4.12.0")
//    "watchImplementation"("androidx.wear.compose:compose-foundation:1.3.1")
//    runtimeOnly("androidx.lifecycle:lifecycle-runtime:2.8.0")
//    val channel = "omni"
    val channel = "arm64-v8a"
//    val channel = "armeabi-v7a"
    implementation("org.mozilla.geckoview:geckoview-${channel}:126.0.20240526221752")
}
