# Local Infra Plugin

This project provides a gradle plugin to manage the local infrastructure of your project. It provides gradle tasks to
start and stop a docker-compose files.

It also enables you to dynamically get hosts from kubernetes by service name to port forward your cloud infrastructure
for local development (for example with docker image port-forward).

## Quickstart

Add the plugin

``` id("io.github.mics21.local-infra-plugin") ```

to your gradle build file.

## Configuration

```
localInfra {
    dockerComposeProjectName = ... 
    testDatabaseName = ...
    dbUser = ...
    dbPassword = ...
    hostNameMapping = ...
}
```

| property                 | default value | description                                                                                              |
|--------------------------|---------------|----------------------------------------------------------------------------------------------------------|
| dockerComposeProjectName |               | location of docker-compose file (relative to src)                                                        |
| testDatabaseName         |               | name of the db which is used for connection check                                                        |
| dbUser                   | postgres      | db user for connection check                                                                             |
| dbPassword               | postgres      | db password for connection check                                                                         |
| hostNameMapping          |               | (optional) mapping from envName used in docker-compose file to service name from cluster to get host url |
| kubeconfig               |               | (optional) the kubeconfig file to use eg: `System.getProperty("user.home") + "/.kube/config-my"`

## Registered Tasks

| name              | description                                                                  |
|-------------------|------------------------------------------------------------------------------|
| startLocalInfra   | starts the local infrastructure                                              |
| stopLocalInfra    | stops the local infrastructure                                               |
| restartLocalInfra | restarts the local infrastructure                                            |
| removeBuildImages | removes images which have been created by the deployment plugin during build |
