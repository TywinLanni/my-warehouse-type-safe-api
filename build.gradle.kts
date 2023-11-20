plugins {
    alias(libs.plugins.kotlin)
}

repositories {
    mavenCentral()
}

group = "com.github.tywinlanni"
version = "0.0.1"

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    kotlin {
        jvmToolchain(8)
    }
}

tasks.test {
    useJUnitPlatform()
}
