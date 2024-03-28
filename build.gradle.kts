import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
}

group = "E6EO"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-web-services")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("com.google.api-client:google-api-client:2.3.0")
    implementation("com.google.oauth-client:google-oauth-client-assembly:1.35.0")
    implementation("org.apache.camel:camel-google-calendar:4.3.0")
    implementation("com.google.apis:google-api-services-people:v1-rev20230103-2.0.0")
    implementation("com.google.apis:google-api-services-tasks:v1-rev20210709-2.0.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

val frontendDir = File("$projectDir/src/main/reactfront")

sourceSets {
    main {
        resources.srcDirs("$projectDir/src/main/resources")
    }
}

tasks.processResources {
    dependsOn("copyReactBuildFiles")
}

val installReact = tasks.register<Exec>("installReact") {
    workingDir = frontendDir
    inputs.dir(frontendDir)
    group = BasePlugin.BUILD_GROUP
    val osName = System.getProperty("os.name").lowercase(Locale.ROOT)
    if (osName.contains("windows")) {
        commandLine("npm.cmd", "audit", "fix")
        commandLine("npm.cmd", "install")
    } else {
        commandLine("npm", "audit", "fix")
        commandLine("npm", "install")
    }
}

val buildReact = tasks.register<Exec>("buildReact") {
    dependsOn(installReact)
    workingDir = frontendDir
    inputs.dir(frontendDir)
    group = BasePlugin.BUILD_GROUP
    val osName = System.getProperty("os.name").lowercase(Locale.ROOT)
    if (osName.contains("windows")) {
        commandLine("npm.cmd", "run-script", "build")
    } else {
        commandLine("npm", "run-script", "build")
    }
}

val copyReactBuildFiles = tasks.register<Copy>("copyReactBuildFiles") {
    dependsOn(buildReact)
    from("$frontendDir/build")
    into("$projectDir/src/main/resources/static")
}