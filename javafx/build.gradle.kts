plugins {
    kotlin("jvm")
}

val kotlinVersion: String by project
val serializationVersion: String by project
val slf4jVersion: String by project
val kotestVersion: String by project
val mockkVersion: String by project

group = "dev.neoancient.ttgamelib"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    runtimeOnly("org.slf4j:slf4j-simple:$slf4jVersion")

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

kotlin {
    explicitApi()
}
