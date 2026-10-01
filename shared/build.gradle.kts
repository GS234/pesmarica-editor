import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room3)
}

kotlin {
    jvm()
    
    // js {
    //     browser()
    //     binaries.executable()
    // }
    //
    // @OptIn(ExperimentalWasmDsl::class)
    // wasmJs {
    //     browser()
    //     binaries.executable()
    // }
    
    android {
       namespace = "org.mpp.dbuild.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            // implementation(libs.androidx.room3.sqlite.wrapper) // when using Room sqlite wrapper
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // room:
            implementation(libs.androidx.room3.runtime)
            implementation(libs.androidx.sqlite.bundled)
            // filekit:
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            // datastore
            implementation(libs.datastore.preferences)
            implementation(libs.datastore)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(libs.compose.uiTooling)
        }
        // jsMain.dependencies {
        //     implementation(libs.wrappers.browser)
        // }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    // room
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspJvm", libs.androidx.room3.compiler)
    // add("kspWasmJs", libs.androidx.room3.compiler)
}


room3 {
    schemaDirectory("$projectDir/schemas")
}
