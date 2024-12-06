pluginManagement {
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.mavenCentral()
        it.maven("https://storage.googleapis.com/gradleup/m2")
    }
}

include("apollo-mockserver")
