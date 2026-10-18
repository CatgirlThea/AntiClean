plugins {
    java
}

group = "com.anticlean"
version = "1.0.4"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.release = 21
    options.encoding = "UTF-8"
}

tasks.jar {
    archiveBaseName = "AntiClean"
    archiveVersion = project.version.toString()
}
