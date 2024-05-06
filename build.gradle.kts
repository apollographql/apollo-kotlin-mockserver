import com.gradleup.librarian.core.configureBcv

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("build-logic:build-logic")
    }
}

configureBcv()
