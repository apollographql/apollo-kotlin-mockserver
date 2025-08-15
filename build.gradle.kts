import com.gradleup.librarian.gradle.Librarian

plugins {
    alias(libs.plugins.kgp).apply(false)
    id("com.gradleup.librarian").version("0.1.1-SNAPSHOT-e64004307778d825d359d0a6d1243f7d16ca2653").apply(false)
    id("org.jetbrains.kotlinx.binary-compatibility-validator").version("0.18.1")
}

Librarian.root(project)
