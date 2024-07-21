pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/google/")

        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://maven.google.com")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/google/")

        maven("https://jitpack.io")
        maven("https://maven.google.com")
        mavenCentral()
    }
}

rootProject.name = "RexFileDemo"
include(":app")
include(":RexFile")
project(":RexFile").name = "rex-file"
