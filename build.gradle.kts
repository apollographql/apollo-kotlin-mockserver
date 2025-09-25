import com.gradleup.librarian.gradle.Librarian

plugins {
  alias(libs.plugins.kgp).apply(false)
  alias(libs.plugins.librarian).apply(false)
  alias(libs.plugins.nmcp).apply(false)
}

Librarian.root(project)
