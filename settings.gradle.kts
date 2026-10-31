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
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/trustwallet/wallet-core")
            // GitHub Packages requires a classic PAT with read:packages (fine-grained tokens are rejected).
            // Set gpr.user / gpr.key in ~/.gradle/gradle.properties, or GPR_USER / GPR_KEY in the environment.
            credentials {
                username = providers.gradleProperty("gpr.user").orElse(providers.environmentVariable("GPR_USER")).orNull
                password = providers.gradleProperty("gpr.key").orElse(providers.environmentVariable("GPR_KEY")).orNull
            }
        }
    }
}


rootProject.name = "Ezipay Coin"
include(":app")
 