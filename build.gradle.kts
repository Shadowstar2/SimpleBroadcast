plugins {
    id("com.gradleup.shadow") version "9.4.2" apply false
    kotlin("jvm") version "2.1.0" apply false
    kotlin("kapt") version "2.1.0" apply false
}

subprojects {
    group = "dev.announce"
    version = "1.2.0"
}
