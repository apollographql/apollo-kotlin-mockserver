import com.gradleup.librarian.core.SonatypeHost
import com.gradleup.librarian.core.configureDokkatooHtml
import com.gradleup.librarian.core.configureDokkatooModule
import com.gradleup.librarian.core.configureJavaVersion
import com.gradleup.librarian.core.configureKotlinVersion
import com.gradleup.librarian.core.configurePublishing
import com.gradleup.librarian.core.pomMetadataFromGradleProperties
import com.gradleup.librarian.core.signingFromEnvironmentVariables
import com.gradleup.librarian.core.sonatypeFromEnvironmentVariables
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
  id("org.jetbrains.kotlin.multiplatform")
}

configureJavaVersion(8)
configureKotlinVersion("1.9.23")
configureDokkatooHtml()
configureDokkatooModule("kdoc")
configurePublishing(
    pomMetadata = pomMetadataFromGradleProperties(),
    sonatype = sonatypeFromEnvironmentVariables(SonatypeHost.S01),
    signing = signingFromEnvironmentVariables()
)

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
  @OptIn(ExperimentalWasmDsl::class)
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

