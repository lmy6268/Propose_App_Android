pluginManagement {
    includeBuild("build-logic") //Configure build-logic in project
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        flatDir {
            dirs(rootDir.resolve("libs"))
        }
    }
}

//https://proandroiddev.com/using-type-safe-project-dependencies-on-gradle-493ab7337aa
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs(rootDir.resolve("libs"))
        }
    }

}
rootProject.name = "ProPoseApplication"
include(":app")
include(":data")
include(":domain")
include(":utils")
include(":presentation")
include(":opencv")
include(":core:designsystem")
include(":core:ui")
