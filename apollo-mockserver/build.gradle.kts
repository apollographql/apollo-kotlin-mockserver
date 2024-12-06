import com.gradleup.librarian.gradle.librarianModule
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  id("org.jetbrains.kotlin.multiplatform")
}

librarianModule(true)

kotlin {
  jvm()
  macosX64()
  macosArm64()
  iosArm64()
  iosX64()
  iosSimulatorArm64()
  watchosArm32()
  watchosArm64()
  watchosSimulatorArm64()
  tvosArm64()
  tvosX64()
  tvosSimulatorArm64()
  js(IR) {
    nodejs()
  }
  @Suppress("OPT_IN_USAGE")
  wasmJs {
    nodejs()
  }

  @OptIn(ExperimentalKotlinGradlePluginApi::class)
  applyDefaultHierarchyTemplate {
    group("common") {
      group("noWasm") {
        group("concurrent") {
          group("apple")
          withJvm()
        }
        withJvm()
      }
    }
  }

  sourceSets {
    findByName("commonMain")?.apply {
      dependencies {
        api(libs.okio)
        implementation(libs.atomicfu.library.get().toString()) {
          because("We need locks for native (we don't use the gradle plugin rewrite)")
        }
        api(libs.kotlinx.coroutines.core)
      }
    }

    findByName("jsMain")?.apply {
      dependencies {
        implementation(libs.kotlin.node)
      }
    }

    findByName("commonTest")?.apply {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }
    findByName("noWasmTest")?.apply {
      dependencies {
        implementation(libs.ktor.client.core)
      }
    }
    findByName("concurrentTest")?.apply {
      dependencies {
        implementation(libs.ktor.client.cio)
      }
    }
    findByName("jsTest")?.apply {
      dependencies {
        implementation(libs.ktor.client.js)
      }
    }
    findByName("wasmJsMain")!!.apply {
      dependencies {
        implementation(libs.kotlin.stdlib.wasm.js)
      }
    }

    findByName("jvmTest")?.apply {
      dependencies {
        implementation(libs.okhttp)
      }
    }

    findByName("concurrentMain")?.apply {
      dependencies {
        implementation(libs.ktor.server.core)
        implementation(libs.ktor.server.cio)
        implementation(libs.ktor.server.websockets)
        implementation(libs.ktor.network)
      }
    }
  }
}

