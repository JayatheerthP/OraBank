package com.jayatheerth.userservice.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.jayatheerth.userservice.exception.UserServiceException;

/**
 * Configuration class for setting up Kafka producer beans.
 * This class defines the necessary configurations for connecting to a Kafka
 * cluster
 * and creating a KafkaTemplate for sending messages.
 */
@Configuration
public class KafkaConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    // Inject Kafka bootstrap servers from application properties
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Creates a ProducerFactory for Kafka with configured properties.
     * This method sets up the connection details and serialization configurations
     * for the Kafka producer.
     *
     * @return ProducerFactory configured for String key and value types.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        try {
            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            // Additional configurations for reliability
            configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
            configProps.put(ProducerConfig.ACKS_CONFIG, "all");
            configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

            logger.info("Kafka ProducerFactory configured successfully with bootstrap servers: {}", bootstrapServers);
            return new DefaultKafkaProducerFactory<>(configProps);
        } catch (Exception e) {
            logger.error("Error configuring Kafka ProducerFactory: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to configure Kafka ProducerFactory", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates a KafkaTemplate for sending messages to Kafka topics.
     * This template uses the configured ProducerFactory to interact with the Kafka
     * cluster.
     *
     * @return KafkaTemplate for String key and value types.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        try {
            KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory());
            logger.info("KafkaTemplate bean created successfully");
            return template;
        } catch (Exception e) {
            logger.error("Error creating KafkaTemplate: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to create KafkaTemplate", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}