plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

val kotlinVersion: String by project
val serializationVersion: String by project
val slf4jVersion: String by project
val junitVersion: String by project
val mockitoVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    runtimeOnly("org.slf4j:slf4j-simple:$slf4jVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
}
