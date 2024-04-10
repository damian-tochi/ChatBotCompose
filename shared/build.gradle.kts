plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui)
            implementation(libs.androidx.activity.compose)
            implementation(libs.decompose)
            implementation(libs.ktor.client.okhttp)
            api(libs.androidx.appcompat)
            api(libs.androidx.core.ktx)

            implementation(libs.generativeai)

        }

        commonMain.dependencies {
            implementation(libs.generativeai)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx)
            implementation(libs.moko.mvvm.compose)
            implementation(libs.kamel)
            implementation(libs.decompose)
            implementation(libs.decompose.jetbrains)
            implementation(libs.squareup.okio)
            implementation(libs.coroutines)
            implementation(libs.voyager.navigator)

            // Screen Model
            implementation(libs.voyager.screenmodel)
            // BottomSheetNavigator
            implementation(libs.voyager.bottom.sheet.navigator)
            // TabNavigator
            implementation(libs.voyager.tab.navigator)
            // Transitions
            implementation(libs.voyager.transitions)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.example.ownchatbot"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }

}
