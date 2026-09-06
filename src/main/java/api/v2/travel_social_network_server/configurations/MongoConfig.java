package api.v2.travel_social_network_server.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB Configuration
 * Simplified configuration - relies on Spring Boot auto-configuration
 * Connection details are specified in application.properties:
 * - spring.data.mongodb.uri
 * - spring.data.mongodb.database
 */
@Configuration
@EnableMongoRepositories(basePackages = "api.v2.travel_social_network_server.repositories.mongodb")
public class MongoConfig {
    // Spring Boot auto-configuration handles:
    // - MongoClient creation from URI
    // - MongoTemplate bean
    // - Default converters (including UUID support)
    // - Connection pooling and settings from URI parameters
}
