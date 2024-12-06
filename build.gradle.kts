import com.gradleup.librarian.gradle.librarianRoot

plugins {
    alias(libs.plugins.kgp).apply(false)
    id("com.gradleup.librarian").version("0.0.6").apply(false)
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.16.3" apply false
}

librarianRoot()
