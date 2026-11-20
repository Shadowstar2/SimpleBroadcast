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
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    implementation("org.yaml:snakeyaml:2.2")
}

tasks {
    shadowJar {
        archiveBaseName.set("SimpleBroadcast-Velocity")
        archiveClassifier.set("")
        relocate("org.yaml.snakeyaml", "dev.announce.simplebroadcast.velocity.libs.snakeyaml")
    }
    jar {
        archiveBaseName.set("SimpleBroadcast-Velocity")
    }
    build {
        dependsOn(shadowJar)
    }
}
