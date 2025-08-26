plugins {
    java
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.mart"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // Apache Camel
    implementation("org.apache.camel.springboot:camel-spring-boot-starter:4.14.0")
    implementation("org.apache.camel.springboot:camel-http-starter:4.14.0")
    implementation("org.apache.camel.springboot:camel-jackson-starter:4.14.0")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis:3.5.0")
    implementation("org.apache.camel:camel-spring-redis:4.14.0")

    // Jackson
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.apache.camel:camel-test-spring-junit5:4.14.0")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    testImplementation("org.springframework.boot:spring-boot-testcontainers:3.5.0")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.testcontainers:testcontainers:1.20.4")
    testImplementation("com.redis.testcontainers:testcontainers-redis:1.6.4")

}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.compileJava {
    options.compilerArgs.add("-parameters")
}