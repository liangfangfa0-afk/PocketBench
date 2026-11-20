plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.ljj.PocketBench"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.ljj.PocketBench"
        minSdk = 34
        targetSdk = 36
        versionCode = 9
        versionName = "1.0.0.10"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation("io.github.itoseo:coui:0.0.1-alpha12")
    implementation("io.github.itoseo:coui-icons:0.0.1-alpha12")
    implementation(platform("androidx.compose:compose-bom-alpha:2026.08.01"))
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.activity:activity-compose:1.13.0")
}
