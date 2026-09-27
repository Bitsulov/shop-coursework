plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "REST API интернет-магазина электроники и бытовой техники"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
	// REST-контроллеры
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	// Валидация входящих данных
	implementation("org.springframework.boot:spring-boot-starter-validation")
	// Безопасность: цепочка фильтров, роли, BCrypt
	implementation("org.springframework.boot:spring-boot-starter-security")
	// JWT-токены
	implementation("io.jsonwebtoken:jjwt-api:0.13.0")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
	// JPA/Hibernate ORM
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	// Миграции БД
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	runtimeOnly("org.flywaydb:flyway-database-postgresql")
	// Драйвер PostgreSQL
	runtimeOnly("org.postgresql:postgresql")
	// Клиент S3-совместимого хранилища
	implementation("io.minio:minio:9.0.3")
	// Обработка изображений
	implementation("net.coobird:thumbnailator:0.4.21")
	// Отправка писем (уведомления о заказах)
	implementation("org.springframework.boot:spring-boot-starter-mail")
	// Обмен сообщениями по протоколу AMQP (RabbitMQ)
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	// OpenAPI/Swagger UI
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")
	// Генерация геттеров, сеттеров, конструкторов
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	// Преобразование сущностей и DTO
	implementation("org.mapstruct:mapstruct:1.6.3")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
	// Связка Lombok и MapStruct: MapStruct видит сгенерированные Lombok геттеры/сеттеры
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
	// Тесты
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-mail-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	// Контейнеры для тестов
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs("-javaagent:${mockitoAgent.asPath}")
	testLogging {
		events("passed", "skipped", "failed")
	}
}

tasks.bootJar {
	archiveFileName = "app.jar"
}
