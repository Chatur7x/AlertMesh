plugins {
    id("org.jetbrains.kotlin.jvm")
    id("application")
    id("org.jetbrains.kotlin.plugin.serialization")
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}

application {
    mainClass.set("com.alertmesh.server.ServerCliKt")
}

tasks.withType<JavaExec> {
    standardInput = System.`in`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
