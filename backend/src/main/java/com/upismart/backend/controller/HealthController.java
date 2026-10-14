package com.upismart.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "UPISmart 2.0 Backend");
        health.put("framework", "Spring Boot 3.2.5 (Java 17/21)");
        health.put("activeProfiles", Arrays.toString(environment.getActiveProfiles()));
        health.put("timestamp", System.currentTimeMillis());

        Map<String, Object> dbInfo = new HashMap<>();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                dbInfo.put("connected", true);
                dbInfo.put("databaseProductName", meta.getDatabaseProductName());
                dbInfo.put("databaseProductVersion", meta.getDatabaseProductVersion());
                dbInfo.put("url", meta.getURL());
            } catch (Exception e) {
                dbInfo.put("connected", false);
                dbInfo.put("error", e.getMessage());
            }
        } else {
            dbInfo.put("connected", false);
            dbInfo.put("reason", "DataSource bean not configured");
        }
        health.put("database", dbInfo);

        return ResponseEntity.ok(health);
    }
}
