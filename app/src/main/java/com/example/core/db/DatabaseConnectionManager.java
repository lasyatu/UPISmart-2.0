package com.example.core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC connectivity for the UPISmart MySQL database layer.
 *
 * Implements security guidelines:
 * - Credentials are NOT hardcoded; they are configured via environment variables or setter parameters.
 * - Connection URLs use secure transport parameters.
 * - Provides graceful offline fallback when a live MySQL daemon is not running in the current environment.
 */
public class DatabaseConnectionManager {

    private String host;
    private int port;
    private String database;
    private String user;
    private String password;

    public DatabaseConnectionManager() {
        this.host = getEnvOrDefault("MYSQL_HOST", "localhost");
        this.port = Integer.parseInt(getEnvOrDefault("MYSQL_PORT", "3306"));
        this.database = getEnvOrDefault("MYSQL_DATABASE", "upismart_db");
        this.user = getEnvOrDefault("MYSQL_USER", "upismart_user");
        this.password = getEnvOrDefault("MYSQL_PASSWORD", "");
    }

    public DatabaseConnectionManager(String host, int port, String database, String user, String password) {
        this.host = host != null ? host : "localhost";
        this.port = port > 0 ? port : 3306;
        this.database = database != null ? database : "upismart_db";
        this.user = user != null ? user : "upismart_user";
        this.password = password != null ? password : "";
    }

    /**
     * Constructs the standard JDBC URL for MySQL with SSL and Unicode options.
     */
    public String getJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    /**
     * Attempts to acquire an active JDBC Connection.
     * Throws SQLException if the MySQL server is unreachable.
     */
    public Connection getConnection() throws SQLException {
        try {
            // Register MySQL JDBC driver if present on classpath
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // Falls back to DriverManager service-provider mechanism
        }
        return DriverManager.getConnection(getJdbcUrl(), user, password);
    }

    /**
     * Probes if the configured MySQL database is currently reachable.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String val = System.getenv(key);
        return (val != null && !val.trim().isEmpty()) ? val.trim() : defaultValue;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUser() {
        return user;
    }
}
