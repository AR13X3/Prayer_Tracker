import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// Read Supabase URL/key from local.properties (gitignored) and expose via BuildConfig.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun secret(name: String): String = localProps.getProperty(name) ?: ""

// Release signing. keystore.properties is gitignored; see keystore.properties.example.
// Absent locally means an unsigned release build (fine for a compile check) — CI always
// writes this file from GitHub Actions secrets before building.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val hasSigningConfig = keystoreProps.getProperty("storeFile") != null

// Public repo (Obtainium's GitHub-Releases source polls this for updates — see README).
val githubRepoUrl = "https://github.com/AR13X3/Prayer_Tracker"

android {
    namespace = "com.prayertracker.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.prayertracker.app"
        minSdk = 26
        targetSdk = 36
        // versionName is also the release tag: pushing tag `vX.Y.Z` must match this exactly
        // (the CI workflow checks and fails the build otherwise). Bump both together —
        // scripts/release.sh does it for you. Obtainium compares versionName against each
        // GitHub Release's tag to decide whether an update is available.
        versionCode = 5
        versionName = "0.2.3"

        // Injected into BuildConfig.SUPABASE_URL / SUPABASE_ANON_KEY at build time.
        buildConfigField("String", "SUPABASE_URL", "\"${secret("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${secret("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "GITHUB_REPO_URL", "\"$githubRepoUrl\"")
    }

    signingConfigs {
        if (hasSigningConfig) {
            create("release") {
                // rootProject.file (not the bare file()) — a relative path here must resolve
                // against android/, matching keystore.properties.example's documented
                // behavior and where the CI workflow writes the decoded keystore. A bare
                // file() call inside this module's build script resolves against android/app/
                // instead, which only an absolute storeFile path (e.g. local dev) papers over.
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // enable later with a tested proguard config
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasSigningConfig) signingConfig = signingConfigs.getByName("release")
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
}

// Kotlin 2.x replaced android { kotlinOptions { } } with the compilerOptions DSL.
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // supabase-kt: BOM pins the module versions; add a Ktor engine for Android.
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.ktor.client.android)

    // On-device prayer-time calculation.
    implementation(libs.adhan2)

    // Offline cache + sync outbox.
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
