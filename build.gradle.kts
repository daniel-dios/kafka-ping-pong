import org.gradle.api.JavaVersion.VERSION_15
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

plugins {
    id("org.springframework.boot") version "2.4.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    java
}

dependencyManagement{
    imports{
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2020.0.4")
    }
}

dependencies {
    // db
    implementation("org.postgresql:postgresql:42.3.1")
    implementation("org.flywaydb:flyway-core:9.8.1")
    implementation("org.springframework:spring-jdbc:5.3.13")

    // cloud
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")

    // spring boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // tests
    testImplementation("org.testcontainers:testcontainers:1.15.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.cloud:spring-cloud-stream-binder-test:3.1.3")
    testImplementation("org.awaitility:awaitility:4.1.1")
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
