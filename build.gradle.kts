import com.gradleup.librarian.gradle.librarianRoot

plugins {
    alias(libs.plugins.kgp).apply(false)
    id("com.gradleup.librarian").version("0.0.8-SNAPSHOT-cd822254de75426f8f047a50bae467da872cc912").apply(false)
}

librarianRoot()
