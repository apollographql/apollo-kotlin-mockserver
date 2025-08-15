import com.gradleup.librarian.gradle.Librarian

plugins {
  alias(libs.plugins.kgp).apply(false)
  id("com.gradleup.librarian").version("0.1.1-SNAPSHOT-e64004307778d825d359d0a6d1243f7d16ca2653").apply(false)
  id("org.jetbrains.kotlinx.binary-compatibility-validator").version("0.18.1")
  id("com.gradleup.nmcp").version("1.0.4-SNAPSHOT-aa74448010e9ec667068e89bdd97ed5edbdf059b")
}

Librarian.root(project)
