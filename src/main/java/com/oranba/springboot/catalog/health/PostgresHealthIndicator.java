package com.oranba.springboot.catalog.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("dbHealthIndicator")
public class PostgresHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public PostgresHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            // Execute a simple query to check if the database is up
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                return Health.up()
                        .withDetail("Database", "PostgreSQL")
                        .withDetail("Status", "Available")
                        .build();
            } else {
                return Health.down()
                        .withDetail("Database", "PostgreSQL")
                        .withDetail("Status", "Query failed")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("Database", "PostgreSQL")
                    .withDetail("Status", "Unavailable")
                    .withDetail("Error", e.getMessage())
                    .build();
        }
    }
}
