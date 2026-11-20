plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.gradleup.shadow")
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    implementation("org.yaml:snakeyaml:2.2")
    implementation(kotlin("stdlib"))
}

tasks {
    shadowJar {
        archiveBaseName.set("SimpleBroadcast-Velocity")
        archiveClassifier.set("")
        relocate("org.yaml.snakeyaml", "dev.announce.simplebroadcast.velocity.libs.snakeyaml")
        relocate("kotlin", "dev.announce.simplebroadcast.libs.kotlin")
    }
    jar {
        archiveBaseName.set("SimpleBroadcast-Velocity")
        archiveClassifier.set("plain")
    }
    build {
        dependsOn(shadowJar)
    }
}
