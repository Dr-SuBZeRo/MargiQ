import java.util.Properties
import java.io.FileInputStream
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

android {
    namespace = "com.margiq"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.margiq"
        minSdk = 26
        targetSdk = 36
        versionCode = 7
        versionName = "2.0.1"
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = listOf(
                rootProject.file("keystore.properties"),
                project.file("keystore.properties")
            ).firstOrNull { it.exists() }
            val keystoreProperties = Properties()
            if (keystorePropertiesFile != null && keystorePropertiesFile.exists()) {
                FileInputStream(keystorePropertiesFile).use { stream ->
                    keystoreProperties.load(stream)
                }
            }

            val storeFilePath = System.getenv("MARGIQ_KEYSTORE_PATH")
                ?: keystoreProperties.getProperty("MARGIQ_KEYSTORE_PATH")
                ?: keystoreProperties.getProperty("storeFile")

            val storePasswordVal = System.getenv("MARGIQ_KEYSTORE_PASSWORD")
                ?: keystoreProperties.getProperty("MARGIQ_KEYSTORE_PASSWORD")
                ?: keystoreProperties.getProperty("storePassword")

            val keyAliasVal = System.getenv("MARGIQ_KEY_ALIAS")
                ?: keystoreProperties.getProperty("MARGIQ_KEY_ALIAS")
                ?: keystoreProperties.getProperty("keyAlias")

            val keyPasswordVal = System.getenv("MARGIQ_KEY_PASSWORD")
                ?: keystoreProperties.getProperty("MARGIQ_KEY_PASSWORD")
                ?: keystoreProperties.getProperty("keyPassword")

            val isReleaseTaskRequested = gradle.startParameter.taskNames.any {
                it.contains("Release", ignoreCase = true)
            }

            if (!storeFilePath.isNullOrBlank() &&
                !storePasswordVal.isNullOrBlank() &&
                !keyAliasVal.isNullOrBlank() &&
                !keyPasswordVal.isNullOrBlank()
            ) {
                storeFile = file(storeFilePath)
                storePassword = storePasswordVal
                keyAlias = keyAliasVal
                keyPassword = keyPasswordVal
            } else if (isReleaseTaskRequested) {
                throw org.gradle.api.GradleException(
                    "\n[MargiQ Security Error] Release signing credentials are missing!\n" +
                    "Please provide credentials via environment variables:\n" +
                    "  - MARGIQ_KEYSTORE_PATH\n" +
                    "  - MARGIQ_KEYSTORE_PASSWORD\n" +
                    "  - MARGIQ_KEY_ALIAS\n" +
                    "  - MARGIQ_KEY_PASSWORD\n" +
                    "Or in an untracked 'keystore.properties' file in the project root.\n" +
                    "Release builds CANNOT fall back to debug signing."
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    lint {
        checkReleaseBuilds = false
        warningsAsErrors = false
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

tasks.withType<Test>().configureEach {
    maxParallelForks = 1
    forkEvery = 0
    maxHeapSize = "192m"
    jvmArgs("-XX:+UseSerialGC")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)

  // Room Database
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)

  // DataStore Preferences
  implementation(libs.androidx.datastore.preferences)

  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner, SQLite JDBC
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation("org.xerial:sqlite-jdbc:3.45.1.0")

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
}

tasks.withType<Test>().configureEach {
    maxParallelForks = 1
    maxHeapSize = "128m"
    jvmArgs(
        "-XX:+UseSerialGC",
        "-Xshare:off",
        "-XX:TieredStopAtLevel=1",
        "-XX:CICompilerCount=1"
    )
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

tasks.withType<JavaCompile>().configureEach {
    options.isFork = false
}
