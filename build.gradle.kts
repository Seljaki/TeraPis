plugins {
    kotlin("jvm") version "1.9.22"
}

group = "si.seljaki"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(kotlin("test"))

    implementation("org.locationtech.proj4j:proj4j:1.3.0")
    implementation("org.locationtech.jts:jts-core:1.18.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(18)
}
