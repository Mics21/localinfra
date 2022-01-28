plugins {
    java
    kotlin("jvm") version "1.5.21"
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.15.0"
}

group = "io.github.mics21"
version = "1.0.1"

repositories {
    gradlePluginPortal()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")

    implementation("io.fabric8:kubernetes-client:5.2.1")

}

gradlePlugin {
    plugins {
        create("localInfraPlugin") {
            id = "io.github.mics21.local-infra-plugin"
            implementationClass = "io.github.mics21.localInfraPlugin.LocalInfraPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/Mics21/localinfra"
    vcsUrl = "https://github.com/Mics21/localinfra"
    description = "Local Infra Plugin to start the local infra of a project"

    (plugins) {
        "localInfraPlugin" {
            // id is captured from java-gradle-plugin configuration
            displayName = "Local Infra Plugin"
            tags = mutableListOf("infrastructure", "docker", "docker-compose")
        }
    }

    mavenCoordinates {
        groupId = "io.github.mics21"
        artifactId = "local-infra-plugin"
        version = "1.0.1"
    }
}