package io.github.mics21.localInfraPlugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin to provide local infrastructure for the alf team.
 * It consist of the task startLocalInfra which starts the whole infrastructure via docker-compose and waits till the empty database is up.
 * The task startLocalInfraComplete executes startLocalInfra but after that waits for the complete backup to be imported.
 * stopLocalInfra stops the complete infrastructure.
 */
class LocalInfraPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("localInfra", LocalInfraPluginExtension::class.java)
        // TODO: somehow the values in the extension need to be set! otherwise we could not start the infra
        project.tasks.register("startLocalInfra") {
            it.description = "Starts the local infrastructure (e.g. postgres)"
            it.group = "local"
            it.outputs.upToDateWhen {
                testConnection(
                    extension.testDatabaseName
                ) && project.currentRunningComposeProject() == extension.dockerComposeProjectName
            }
            it.doLast {
                project.startLocalInfra(extension.testDatabaseName, extension.dockerComposeProjectName)
            }
        }

        project.tasks.register("restartLocalInfra") {
            it.doFirst{
                project.stopLocalInfra(extension.dockerComposeProjectName)
            }
            it.doLast {
                project.startLocalInfra(extension.testDatabaseName, extension.dockerComposeProjectName)
            }
        }

        project.tasks.register("stopLocalInfra") {
            it.description = "Stops the local infrastructure (e.g. postgres)"
            it.group = "local"
            it.doFirst {
                project.stopLocalInfra(extension.dockerComposeProjectName)
            }
        }
    }
}