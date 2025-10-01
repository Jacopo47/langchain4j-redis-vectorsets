plugins {
    id("java")
}

group = "org.articioc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.langchain4j:langchain4j:1.5.0")
    implementation("dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:1.5.0-beta11")
    implementation("dev.langchain4j:langchain4j-community:1.5.0-beta11")
    implementation("ai.djl.huggingface:tokenizers:0.34.0")
    implementation("redis.clients:jedis:6.2.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:5.20.0")
    testImplementation("com.redis:testcontainers-redis:2.2.4")
}

tasks.test {
    useJUnitPlatform()
}