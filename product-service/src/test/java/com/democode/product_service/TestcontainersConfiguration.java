package com.democode.product_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mongodb.MongoDBAtlasLocalContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	MongoDBAtlasLocalContainer mongoDbAtlasContainer() {
		return new MongoDBAtlasLocalContainer(DockerImageName.parse("mongodb/mongodb-atlas-local:latest"));
	}

}
