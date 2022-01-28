package io.github.mics21.localInfraPlugin

open class LocalInfraPluginExtension {
    var dockerComposeProjectName: String = ""
    var testDatabaseName: String = ""
    var dbUser: String = "postgres"
    var dbPassword: String = "postgres"
    var hostNameMapping: Map<String, String>? = null
}