plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.schreib"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Include the sdk as a dependency
// x-release-please-start-version
    implementation("com.microsoft.graph:microsoft-graph:6.43.0")
// x-release-please-end
// This dependency is only needed if you are using a TokenCredential object for authentication
    implementation("com.azure:azure-identity:1.15.0")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

}

tasks.withType<Test> {
    useJUnitPlatform()
}
