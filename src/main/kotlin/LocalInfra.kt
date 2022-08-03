package io.github.mics21.localInfraPlugin

import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.gradle.api.Project
import java.io.File
import java.sql.DriverManager

private const val SLEEP_TIME: Long = 2000

internal fun waitOnLocalPostgres(database: String, user: String, password: String) {
    while (!testConnection(database, user, password)) {
        println("Waiting on local Postgres")
        Thread.sleep(SLEEP_TIME)
    }
}

@Suppress("TooGenericExceptionCaught")
internal fun testConnection(database: String, user: String, password: String): Boolean {
    val url = "jdbc:postgresql://localhost:5432/$database?user=$user&password=$password"
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

internal fun Project.startLocalInfra(
    testDbName: String,
    dbUser: String,
    dbPassword: String,
    composeProjectName: String,
    hostNameMapping: Map<String, String>?,
    kubeconfigPath:String?
) {
    println("using build namespace ${buildNamespace()}")
    stopOtherProjects(composeProjectName)
    when {
        !testConnection(testDbName, dbUser, dbPassword) -> {
            println("$testDbName db could not be reached, stopping current infra!")
            stopLocalInfra(composeProjectName)
        }
        testConnection(testDbName, dbUser, dbPassword) -> return
    }

    println("Starting docker compose project: $composeProjectName")
    exec {
        hostNameMapping?.forEach { (key, value) ->
            it.environment(key, getHostForNameMapping(key, value,kubeconfigPath))
        }
            it.commandLine("docker", "compose", "up", "-d", "--remove-orphans")
        it.workingDir("${rootProject.projectDir}/src/$composeProjectName")
    }
    waitOnLocalPostgres(testDbName, dbUser, dbPassword)
}

private fun getHostForNameMapping(envName: String, serviceName: String,kubeconfigPath:String?): String {
    return kubeClient(kubeconfigPath).run {
        services().inNamespace(buildNamespace()).withName(serviceName)
            .get().status.loadBalancer.ingress.first().hostname
    }.also {
        println("using $envName: $it")
    }
}
private fun kubeClient(kubeconfigPath: String?)=kubeconfigPath?.let {
    DefaultKubernetesClient(Config.fromKubeconfig(File(it).readText()))
}?:DefaultKubernetesClient()

internal fun Project.stopLocalInfra(projectName: String) {
    if (currentRunningComposeProject() == null) {
        return
    }
    exec {
        it.commandLine("docker", "compose", "-p", projectName, "down", "--remove-orphans")
    }
}

internal fun Project.removeImages(images: List<String> = emptyList(), ignoreExitValue: Boolean = true) {
    if (images.isNotEmpty()) {
        exec {
            println("Removing images: ${images.joinToString(" ")}")
            it.commandLine("docker", "image", "rm")
            it.args = it.args + images
            it.isIgnoreExitValue = ignoreExitValue
        }
    }
}
