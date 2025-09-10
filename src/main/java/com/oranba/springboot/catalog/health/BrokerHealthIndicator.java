package com.oranba.springboot.catalog.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("kafkaHealthIndicator")
public class BrokerHealthIndicator implements HealthIndicator {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public BrokerHealthIndicator(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Health health() {
        // Implement your health check logic here
        boolean brokerIsUp = checkBrokerStatus();
        if (brokerIsUp) {
            return Health.up().withDetail("Broker", "Available").build();
        } else {
            return Health.down().withDetail("Broker", "Unavailable").build();
        }
    }

    private boolean checkBrokerStatus() {
        try {
            // Get the Kafka broker connection information
            return kafkaTemplate.getProducerFactory().createProducer().metrics().size() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
