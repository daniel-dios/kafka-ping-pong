import Build_gradle.Versions.Companion.springVersion
import org.gradle.api.JavaVersion.VERSION_15
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

plugins {
    id("org.springframework.boot") version "2.4.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    java
}

abstract class Versions : Plugin<Gradle> {
    companion object {
        const val springVersion = "2.4.1"
    }
}

dependencies {
    implementation("org.postgresql:postgresql:42.2.18")
    implementation("org.flywaydb:flyway-core:7.5.0")
    implementation("org.springframework:spring-jdbc:5.3.3")
    implementation("io.springfox:springfox-swagger2:3.0.0")
    implementation("io.springfox:springfox-swagger-ui:3.0.0")
    implementation("org.springframework.boot:spring-boot-starter-web:${springVersion}")
    implementation("org.springframework.boot:spring-boot-autoconfigure:${springVersion}")
    implementation("org.springframework.boot:spring-boot-starter-actuator:${springVersion}")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:${springVersion}")

    testImplementation("org.testcontainers:testcontainers:1.15.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test:${springVersion}")
}

group "com.kafka-ping-pong"

java {
    sourceCompatibility = VERSION_15
}
repositories {
    mavenCentral()
}

tasks {

    test {
        testLogging.showStandardStreams = true
        testLogging.exceptionFormat = FULL
        useJUnitPlatform()
    }
}
