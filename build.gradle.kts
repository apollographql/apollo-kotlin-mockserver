import com.gradleup.librarian.gradle.Librarian

plugins {
    alias(libs.plugins.kgp).apply(false)
    id("com.gradleup.librarian").version("0.1.0").apply(false)
}

Librarian.root(project)
