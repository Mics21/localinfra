package io.github.mics21.localInfraPlugin

fun buildNamespace(): String = if(System.getenv().containsKey("BUILD_NAMESPACE")){
    System.getenv("BUILD_NAMESPACE")
}else{
    System.getProperty("user.name").toLowerCase()
}
