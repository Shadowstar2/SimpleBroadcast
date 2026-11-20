plugins {
    java
    id("com.gradleup.shadow")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

tasks {
    shadowJar {
        archiveBaseName.set("SimpleBroadcast-Paper")
        archiveClassifier.set("")
    }
    jar {
        archiveBaseName.set("SimpleBroadcast-Paper")
    }
    build {
        dependsOn(shadowJar)
    }
}
