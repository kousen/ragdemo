plugins {
    java
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "edu.trincoll"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// Explicitly set main class to avoid ASM class scanning issue with Java 25
springBoot {
    mainClass = "edu.trincoll.ragdemo.RagDemoApplication"
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:1.0.3")
    }
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")

    // Spring AI - OpenAI integration
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // Spring AI - RAG advisors (includes QuestionAnswerAdvisor and SimpleVectorStore)
    implementation("org.springframework.ai:spring-ai-advisors-vector-store")

    // Spring AI - PDF document reader
    implementation("org.springframework.ai:spring-ai-pdf-document-reader")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Enable stdin for interactive CLI
tasks.named<JavaExec>("bootRun") {
    standardInput = System.`in`
}
