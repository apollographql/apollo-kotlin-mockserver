import com.gradleup.librarian.gradle.Librarian

plugins {
  alias(libs.plugins.kgp).apply(false)
  id("com.gradleup.librarian").version("0.0.8-SNAPSHOT-b703634bb1858f471503e23d2c23d7aeae9d6120").apply(false)
}

Librarian.root(project)
