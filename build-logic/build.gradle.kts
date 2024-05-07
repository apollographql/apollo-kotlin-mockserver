plugins {
    `embedded-kotlin`
}

dependencies {
    implementation(libs.kgp)
    implementation(libs.librarian)
    implementation("com.gradleup.maven-sympathy:maven-sympathy:0.0.1")
}

group = "build-logic"