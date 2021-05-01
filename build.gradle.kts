import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.4.10" apply false
    kotlin("plugin.serialization") version "1.4.10" apply false
}

allprojects {
    group = "dev.neoancient.ttgamelib"
    version = "0.1-SNAPSHOT"

    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

subprojects {
    repositories {
        mavenCentral()
    }
}
