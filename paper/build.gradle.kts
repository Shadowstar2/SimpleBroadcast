plugins {
    kotlin("jvm")
    id("com.gradleup.shadow")
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation(kotlin("stdlib"))
}

tasks {
    shadowJar {
        archiveBaseName.set("SimpleBroadcast-Paper")
        archiveClassifier.set("")
        relocate("kotlin", "dev.announce.simplebroadcast.libs.kotlin")
    }
    jar {
        archiveBaseName.set("SimpleBroadcast-Paper")
        archiveClassifier.set("plain")
    }
    build {
        dependsOn(shadowJar)
    }
}
