import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsPlugin
import com.google.gms.googleservices.GoogleServicesPlugin
import java.io.BufferedReader
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kapt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.navigation)
}

val packageName = "org.farcom.app"
val useDifferentPackageNameForDebugBuild = false

val sdkPath = providers.gradleProperty("LinphoneSdkBuildDir").get()
val googleServices = File(projectDir.absolutePath + "/google-services.json")
val linphoneLibs = File("$sdkPath/libs/")
val linphoneDebugLibs = File("$sdkPath/libs-debug/")
val firebaseCloudMessagingAvailable = googleServices.exists()
val crashlyticsAvailable = googleServices.exists() && linphoneLibs.exists() && linphoneDebugLibs.exists()

if (firebaseCloudMessagingAvailable) {
    println("google-services.json found, enabling Firebase CloudMessaging feature")
    apply<GoogleServicesPlugin>()
} else {
    println("google-services.json not found, disabling Firebase CloudMessaging feature")
}
if (crashlyticsAvailable) {
    println("google-services.json found and Linphone SDK libs-debug folder found, enabling Crashlytics feature")
    apply<CrashlyticsPlugin>()
} else {
    println("Crashlytics has been disabled because either google-services.json file wasn't found or local Linphone SDK build folder isn't configured")
}

var gitVersion = "6.2.3"
var gitBranch = ""
try {
    val gitDescribe = ProcessBuilder()
        .command("git", "describe", "--abbrev=0")
        .directory(project.rootDir)
        .start()
        .inputStream.bufferedReader().use(BufferedReader::readText)
        .trim()
    println("Git describe: $gitDescribe")

    val gitCommitsCount = ProcessBuilder()
        .command("git", "rev-list", "$gitDescribe..HEAD", "--count")
        .directory(project.rootDir)
        .start()
        .inputStream.bufferedReader().use(BufferedReader::readText)
        .trim()
    println("Git commits count: $gitCommitsCount")

    val gitCommitHash = ProcessBuilder()
        .command("git", "rev-parse", "--short", "HEAD")
        .directory(project.rootDir)
        .start()
        .inputStream.bufferedReader().use(BufferedReader::readText)
        .trim()
    println("Git commit hash: $gitCommitHash")

    gitBranch = ProcessBuilder()
        .command("git", "name-rev", "--name-only", "HEAD")
        .directory(project.rootDir)
        .start()
        .inputStream.bufferedReader().use(BufferedReader::readText)
        .trim()
    println("Git branch name: $gitBranch")

    gitVersion =
        if (gitCommitsCount.toInt() == 0) {
            gitDescribe
        } else {
            "$gitDescribe.$gitCommitsCount+$gitCommitHash"
        }
} catch (e: Exception) {
    println("Git not found [$e], using $gitVersion")
}
println("Computed git version: $gitVersion")

configurations {
    implementation { isCanBeResolved = true }
}

tasks.register("linphoneSdkSource") {
    doLast {
        configurations.implementation.get().incoming.resolutionResult.allComponents.forEach {
            if (it.id.displayName.contains("linphone-sdk-android")) {
                println("Linphone SDK used is ${it.moduleVersion?.version}")
            }
        }
    }
}
project.tasks.preBuild.dependsOn("linphoneSdkSource")

android {
    namespace = "org.linphone"
    compileSdk = 36

    defaultConfig {
        applicationId = packageName
        minSdk = 28
        targetSdk = 36
        versionCode = 602003 // 6.02.003
        versionName = "6.2.3"

        manifestPlaceholders["appAuthRedirectScheme"] = packageName

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                output.outputFileName = "farcom-android-${variant.buildType.name}-$gitVersion.apk"
            }
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
    } else {
        println("keystore.properties not found, release build will be unsigned")
    }

    signingConfigs {
        create("release") {
            val keyStorePath = keystoreProperties.getProperty("storeFile", "")
            val keyStore = if (keyStorePath.isNotEmpty()) project.file(keyStorePath) else null
            val storePasswordValue = keystoreProperties.getProperty("storePassword", "")
            val keyAliasValue = keystoreProperties.getProperty("keyAlias", "")
            val keyPasswordValue = keystoreProperties.getProperty("keyPassword", "")
            if (keyStore != null && keyStore.exists() &&
                storePasswordValue.isNotEmpty() && keyAliasValue.isNotEmpty() && keyPasswordValue.isNotEmpty()
            ) {
                storeFile = keyStore
                storePassword = storePasswordValue
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
                println("Signing config release is using keystore [$storeFile]")
            } else {
                println("No valid keystore configured, release build will be unsigned (set keystore.properties to sign it)")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            if (useDifferentPackageNameForDebugBuild) {
                applicationIdSuffix = ".debug"
            }
            isDebuggable = true
            isJniDebuggable = true

            val appVersion = gitVersion
            val appBranch = gitBranch
            println("Debug flavor app version is [$appVersion], app branch is [$appBranch]")
            resValue("string", "linphone_app_version", appVersion)
            resValue("string", "linphone_app_branch", appBranch)
            if (useDifferentPackageNameForDebugBuild) {
                resValue("string", "file_provider", "$packageName.debug.fileprovider")
            } else {
                resValue("string", "file_provider", "$packageName.fileprovider")
            }
            resValue("string", "linphone_openid_callback_scheme", packageName)

            if (crashlyticsAvailable) {
                val path = File("$sdkPath/libs-debug/").toString()
                configure<CrashlyticsExtension> {
                    nativeSymbolUploadEnabled = true
                    unstrippedNativeLibsDir = path
                }
            }
            buildConfigField("Boolean", "CRASHLYTICS_ENABLED", crashlyticsAvailable.toString())
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")

            val appVersion = gitVersion
            val appBranch = gitBranch
            println("Release flavor app version is [$appVersion], app branch is [$appBranch]")
            resValue("string", "linphone_app_version", appVersion)
            resValue("string", "linphone_app_branch", appBranch)
            resValue("string", "file_provider", "$packageName.fileprovider")
            resValue("string", "linphone_openid_callback_scheme", packageName)

            if (crashlyticsAvailable) {
                val path = File("$sdkPath/libs-debug/").toString()
                configure<CrashlyticsExtension> {
                    nativeSymbolUploadEnabled = true
                    unstrippedNativeLibsDir = path
                }
            }
            buildConfigField("Boolean", "CRASHLYTICS_ENABLED", crashlyticsAvailable.toString())
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        dataBinding = true
        buildConfig = true
        resValues = true
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.androidx.annotations)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraint.layout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.telecom)
    implementation(libs.androidx.media)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.slidingpanelayout)
    implementation(libs.androidx.window)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.security.crypto.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.car)

    implementation(libs.google.flexbox)
    implementation(libs.google.material)
    implementation(libs.google.protobuf)

    implementation(platform(libs.google.firebase.bom))
    implementation(libs.google.firebase.messaging)
    if (crashlyticsAvailable) {
        implementation(libs.google.firebase.crashlytics)
    } else {
        compileOnly(libs.google.firebase.crashlytics)
    }

    implementation(libs.coil)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.coil.video)
    implementation(libs.dots.indicator)
    implementation(libs.photoview)
    implementation(libs.openid.appauth)

    implementation(libs.linphone)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    android.set(true)
    ignoreFailures.set(true)
    additionalEditorconfig.set(
        mapOf(
            "max_line_length" to "120",
            "ktlint_standard_max-line-length" to "disabled",
            "ktlint_standard_function-signature" to "disabled",
            "ktlint_standard_no-blank-line-before-rbrace" to "disabled",
            "ktlint_standard_no-empty-class-body" to "disabled",
            "ktlint_standard_annotation-spacing" to "disabled",
            "ktlint_standard_class-signature" to "disabled",
            "ktlint_standard_function-expression-body" to "disabled",
            "ktlint_standard_function-type-modifier-spacing" to "disabled",
            "ktlint_standard_if-else-wrapping" to "disabled",
            "ktlint_standard_argument-list-wrapping" to "disabled",
            "ktlint_standard_trailing-comma-on-call-site" to "disabled",
            "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
            "ktlint_standard_no-empty-first-line-in-class-body" to "disabled",
            "ktlint_standard_no-empty-first-line-in-method-block" to "disabled",
            "ktlint_standard_no-trailing-spaces" to "disabled",
            "ktlint_standard_no-blank-line-in-list" to "disabled",
            "ktlint_standard_no-multi-spaces" to "disabled",
            "ktlint_standard_try-catch-finally-spacing" to "disabled",
            "ktlint_standard_block-comment-initial-star-alignment" to "disabled",
            "ktlint_standard_spacing-between-declarations-with-comments" to "disabled",
            "ktlint_standard_no-consecutive-comments" to "disabled",
            "ktlint_standard_multiline-expression-wrapping" to "disabled",
            "ktlint_standard_parameter-list-wrapping" to "disabled",
            "ktlint_standard_comment-wrapping" to "disabled",
            "ktlint_standard_discouraged-comment-location" to "disabled",
            "ktlint_standard_string-template-indent" to "disabled",
            "ktlint_standard_parameter-list-spacing" to "disabled",
            "ktlint_standard_statement-wrapping" to "disabled",
            "ktlint_standard_import-ordering" to "disabled",
            "ktlint_standard_paren-spacing" to "disabled",
            "ktlint_standard_curly-spacing" to "disabled",
            "ktlint_standard_indent" to "disabled",
        )
    )
}
// ktlintFormat is intentionally NOT wired into preBuild.

if (crashlyticsAvailable) {
    afterEvaluate {
        tasks.getByName("assembleDebug").finalizedBy(
            tasks.getByName("uploadCrashlyticsSymbolFileDebug"),
        )
        tasks.getByName("packageDebug").finalizedBy(
            tasks.getByName("uploadCrashlyticsSymbolFileDebug"),
        )
        tasks.getByName("assembleRelease").finalizedBy(
            tasks.getByName("uploadCrashlyticsSymbolFileRelease"),
        )
        tasks.getByName("packageRelease").finalizedBy(
            tasks.getByName("uploadCrashlyticsSymbolFileRelease"),
        )
    }
}
