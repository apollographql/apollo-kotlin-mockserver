pluginManagement {
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.mavenCentral()
    }
}

includeBuild("build-logic")

include("apollo-mockserver")
