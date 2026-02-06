plugins {
    java
    application
}

group = "edu.trincoll"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "edu.trincoll.ragdemo.RagDemo"
}

repositories {
    mavenCentral()
}

val langchain4jVersion = "1.11.0"

dependencies {
    // LangChain4j core
    implementation("dev.langchain4j:langchain4j:$langchain4jVersion")

    // LangChain4j OpenAI integration (chat model + embedding model)
    implementation("dev.langchain4j:langchain4j-open-ai:$langchain4jVersion")

    // Document parsing (Apache Tika-based, handles PDF)
    // Tika parser uses a separate beta versioning scheme
    implementation("dev.langchain4j:langchain4j-document-parser-apache-tika:1.10.0-beta18")

    // SLF4J simple logger (LC4j uses SLF4J internally)
    implementation("org.slf4j:slf4j-simple:2.0.17")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.7")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Enable stdin for interactive CLI
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
