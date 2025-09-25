package com.navigator.db.conection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find database.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading database configuration", e);
        }
    }

    public static String getUrl() {
        return properties.getProperty("db.url");
    }

    public static String getUsername() {
        return properties.getProperty("db.username");
    }

    public static String getPassword() {
        return properties.getProperty("db.password");
    }

    public static int getPoolSize() {
        return Integer.parseInt(properties.getProperty("db.pool.size", "10"));
    }

    public static int getConnectionTimeout() {
        return Integer.parseInt(properties.getProperty("db.connection.timeout", "30"));
    }

    public static int getIdleTimeout() {
        return Integer.parseInt(properties.getProperty("db.idle.timeout", "10"));
    }

    public static int getMaxLifetime() {
        return Integer.parseInt(properties.getProperty("db.max.lifetime", "1800"));
    }
}