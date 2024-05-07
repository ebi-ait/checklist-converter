plugins {
	java
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.4"
}

group = "uk.ac.ebi"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

extra["springCloudVersion"] = "2023.0.1"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
//	implementation("org.springframework.cloud:spring-cloud-function-context")
	implementation("org.springframework.cloud:spring-cloud-starter-function-web")

	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.11.1")
	implementation("com.google.guava:guava:29.0-jre")
	implementation("org.modelmapper:modelmapper:2.3.0")
	implementation("org.apache.velocity:velocity:1.7")
	implementation("org.apache.velocity:velocity-tools:2.0")
	implementation("org.apache.velocity:velocity:1.7")

	annotationProcessor("org.projectlombok:lombok:1.18.30")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
