package io.github.mics21.localInfraPlugin

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.gradle.api.Project
import java.io.File
import java.sql.DriverManager

private const val SLEEP_TIME: Long = 2000

internal fun waitOnLocalPostgres(database: String) {
    while (!testConnection(database)) {
        println("Waiting on local Postgres")
        Thread.sleep(SLEEP_TIME)
    }
}

@Suppress("TooGenericExceptionCaught")
internal fun testConnection(database: String): Boolean {
    val url = "jdbc:postgresql://localhost:5432/$database?user=postgres&password=postgres"
    return try {
        Class.forName("org.postgresql.Driver")
        DriverManager.getConnection(url)
        true
    } catch (e: Throwable) {
        false
    }
}

fun Project.command(cmd: List<String>, workingDirectory: String = ".") =
    java.io.ByteArrayOutputStream().also { stream ->
        println("Running command $cmd")
        exec {
            it.commandLine = cmd
            it.standardOutput = stream
            it.workingDir = File(workingDirectory)
        }
    }.run {
        toString().trim()
    }

internal fun Project.currentRunningComposeProject(): String? {
    val runningComposeProject = command(listOf("docker", "compose", "ls", "-q")).lines().first()
    if (runningComposeProject.isBlank()) {
        return null
    }
    return runningComposeProject
}

internal fun Project.stopOtherProjects(composeProjectName: String) {
    val runningComposeProject = currentRunningComposeProject() ?: return
    if (runningComposeProject !== composeProjectName) {
        println("Found other docker compose project stopping: $runningComposeProject")
        stopLocalInfra(runningComposeProject)
    }
}

internal fun Project.startLocalInfra(testDbName: String, composeProjectName: String) {
    println("using build namespace ${buildNamespace()}")
    stopOtherProjects(composeProjectName)
    when {
        !testConnection(testDbName) -> {
            println("$testDbName db could not be reached, stopping current infra!")
            stopLocalInfra(composeProjectName)
        }
        testConnection(testDbName) -> return
    }

    println("Starting docker compose project: $composeProjectName")
    exec {
        it.environment("REMOTEHOST_POSTGRESQL", getPostgreSQLHost())
        it.environment("REMOTEHOST_ELASTICSEARCH", getElasticSearchHost())
        it.commandLine("docker", "compose", "up", "-d", "--remove-orphans")
        it.workingDir("${rootProject.projectDir}/src/$composeProjectName")
    }
    waitOnLocalPostgres(testDbName)
}

private fun getPostgreSQLHost(): String {
    return DefaultKubernetesClient().run {
        services().inNamespace(buildNamespace()).withName("database-cloud-infra")
            .get().status.loadBalancer.ingress.first().hostname
    }.also {
        println("using database: $it")
    }
}

private fun getElasticSearchHost(): String {
    return DefaultKubernetesClient().run {
        services().inNamespace(buildNamespace()).withName("elasticsearch-cloud-infra")
            .get().status.loadBalancer.ingress.first().hostname
    }.also {
        println("using elasticsearch: $it")
    }
}

internal fun Project.stopLocalInfra(projectName: String) {
    exec {
        it.commandLine("docker", "compose", "-p", projectName, "down", "--remove-orphans")
    }
}