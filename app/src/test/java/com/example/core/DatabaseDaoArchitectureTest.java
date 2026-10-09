package com.example.core;

import com.example.core.db.DatabaseConnectionManager;
import com.example.core.db.UserProfile;
import com.example.core.model.VaultAuditLog;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests verifying JDBC configuration, connection manager parameters,
 * and data mapping models.
 */
public class DatabaseDaoArchitectureTest {

    @Test
    public void testDatabaseConnectionManagerConfiguration() {
        DatabaseConnectionManager manager = new DatabaseConnectionManager(
                "db.upismart.internal",
                3306,
                "upismart_production",
                "upismart_app",
                "secret_pass"
        );

        assertEquals("db.upismart.internal", manager.getHost());
        assertEquals(3306, manager.getPort());
        assertEquals("upismart_production", manager.getDatabase());
        assertEquals("upismart_app", manager.getUser());

        String jdbcUrl = manager.getJdbcUrl();
        assertTrue("JDBC URL must use MySQL protocol", jdbcUrl.startsWith("jdbc:mysql://db.upismart.internal:3306/upismart_production"));
        assertTrue(jdbcUrl.contains("useSSL=false"));
        assertTrue(jdbcUrl.contains("serverTimezone=UTC"));

        // In a test container without MySQL running, testConnection() must safely return false without crashing
        assertFalse(manager.testConnection());
    }

    @Test
    public void testUserProfileMappingModel() {
        UserProfile profile = new UserProfile(
                1L, "Alex Rivera", "alex@demo.upismart", "alex@demo.upismart",
                55000.0, "HASHED_PIN", true
        );

        assertEquals(1L, profile.getUserId());
        assertEquals("Alex Rivera", profile.getUsername());
        assertEquals("alex@demo.upismart", profile.getEmail());
        assertEquals(55000.0, profile.getMonthlySalary(), 0.001);
        assertTrue(profile.isBiometricsEnabled());
        assertNotNull(profile.toString());
    }

    @Test
    public void testVaultAuditLogModel() {
        VaultAuditLog log = new VaultAuditLog(
                System.currentTimeMillis(),
                VaultAuditLog.ActionType.WITHDRAWAL,
                2500.0,
                true,
                "Emergency medical payment",
                12500.0
        );

        assertEquals(VaultAuditLog.ActionType.WITHDRAWAL, log.getActionType());
        assertEquals(2500.0, log.getAmount(), 0.001);
        assertTrue(log.isSuccessful());
        assertEquals("Emergency medical payment", log.getReason());
        assertEquals(12500.0, log.getResultingVaultBalance(), 0.001);
    }
}
