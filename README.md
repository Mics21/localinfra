# Local Infra Plugin

This project provides a gradle plugin to manage the local infrastructure of your project.
It provides gradle tasks to start and stop a docker-compose files.

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
}
```

| property                 | default value | description                                       |
|--------------------------|---------------|---------------------------------------------------|
| dockerComposeProjectName |               | location of docker-compose file (relative to src) |
| testDatabaseName         |               | name of the db which is used for connection check |
| dbUser                   | postgres      | db user for connection check                      |
| dbPassword               | postgres      | db password for connection check                  |

## Registered Tasks

| name              | description                                                                  |
|-------------------|------------------------------------------------------------------------------|
| startLocalInfra   | starts the local infrastructure                                              |
| stopLocalInfra    | stops the local infrastructure                                               |
| restartLocalInfra | restarts the local infrastructure                                            |
| removeBuildImages | removes images which have been created by the deployment plugin during build |
