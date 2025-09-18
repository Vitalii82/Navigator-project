package com.navigator.db.conection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {
    private static final Logger logger = LogManager.getLogger(ConnectionPool.class);
    private static ConnectionPool instance;
    private final BlockingQueue<Connection> connections;
    private final int poolSize;
    private final long timeoutMillis = 6000;

    private ConnectionPool() {
        try {
            logger.debug("Initializing connection pool...");

            this.poolSize = DatabaseConfig.getPoolSize();
            logger.debug("Pool size: {}", poolSize);

            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.debug("MySQL JDBC driver loaded");

            connections = new LinkedBlockingQueue<>(poolSize);
            initializePool();

            logger.info("Connection pool initialized with {} connections", poolSize);

        } catch (ClassNotFoundException e) {
            logger.error("MySQL JDBC driver not found", e);
            ExceptionHandler.handleException(e, "JDBC driver not found");
            throw new RuntimeException("MySQL JDBC driver not found", e);
        } catch (SQLException e) {
            logger.error("Failed to initialize pool", e);
            ExceptionHandler.handleSQLException(e, "connection pool initialization");
            throw new RuntimeException("Failed to initialize pool", e);
        } catch (Exception e) {
            logger.error("Unexpected error during pool initialization", e);
            ExceptionHandler.handleException(e, "connection pool creation");
            throw new RuntimeException("Unexpected error during pool initialization", e);
        }
    }

    private void initializePool() throws SQLException {
        logger.debug("Creating {} connections...", poolSize);

        int successfulConnections = 0;
        for (int i = 0; i < poolSize; i++) {
            try {
                Connection connection = createConnection();
                connections.offer(connection);
                successfulConnections++;
            } catch (SQLException e) {
                logger.warn("Failed to create connection #{}: {}", i + 1, e.getMessage());
            }
        }

        if (successfulConnections == 0) {
            throw new SQLException("No connections created");
        }

        logger.info("Created {} of {} connections", successfulConnections, poolSize);
    }

    private Connection createConnection() throws SQLException {
        try {
            logger.debug("Creating new connection...");

            Connection connection = DriverManager.getConnection(
                    DatabaseConfig.getUrl(),
                    DatabaseConfig.getUsername(),
                    DatabaseConfig.getPassword()
            );

            if (connection.isValid(2)) {
                logger.debug("Connection created successfully");
                return connection;
            } else {
                logger.error("Invalid connection created");
                throw new SQLException("Invalid connection");
            }

        } catch (SQLException e) {
            logger.error("Error creating connection: {}", e.getMessage());
            ExceptionHandler.handleSQLException(e, "create connection");
            throw e;
        }
    }

    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            logger.debug("Creating new pool instance");
            instance = new ConnectionPool();
        } else {
            logger.debug("Using existing pool instance");
        }
        return instance;
    }

    public Connection getConnection() throws InterruptedException {
        logger.debug("Requesting connection. Available: {}", getAvailableConnectionsCount());

        try {
            Connection connection = connections.poll(timeoutMillis, TimeUnit.MILLISECONDS);

            if (connection == null) {
                logger.warn("Timeout after {} ms waiting for connection", timeoutMillis);
                throw new RuntimeException("Timeout waiting for connection");
            }

            if (connection.isClosed() || !connection.isValid(2)) {
                logger.warn("Invalid connection retrieved, creating new one");
                connection = createConnection();
            }

            logger.debug("Connection acquired. Remaining: {}", getAvailableConnectionsCount());
            return connection;

        } catch (SQLException e) {
            logger.error("Failed to get connection: {}", e.getMessage());
            ExceptionHandler.handleSQLException(e, "get connection from pool");
            throw new RuntimeException("Failed to get connection", e);
        }
    }

    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed() && connection.isValid(2)) {
                    connection.setAutoCommit(true);
                    connections.offer(connection);
                    logger.debug("Connection released. Available: {}", getAvailableConnectionsCount());
                } else {
                    logger.warn("Invalid connection returned, creating replacement");
                    connections.offer(createConnection());
                }
            } catch (SQLException e) {
                logger.error("Error releasing connection: {}", e.getMessage());
                ExceptionHandler.handleSQLException(e, "release connection");
                try {
                    connections.offer(createConnection());
                    logger.info("Replacement connection created");
                } catch (SQLException ex) {
                    logger.error("Failed to create replacement: {}", ex.getMessage());
                    ExceptionHandler.handleSQLException(ex, "create replacement connection");
                }
            }
        } else {
            logger.warn("Attempted to release null connection");
        }
    }

    public int getAvailableConnectionsCount() {
        int count = connections.size();
        logger.trace("Available connections: {}", count);
        return count;
    }

    public void closeAllConnections() {
        logger.info("Closing all connections...");

        int closedCount = 0;
        for (Connection connection : connections) {
            if (closeConnectionSilently(connection)) {
                closedCount++;
            }
        }
        connections.clear();

        logger.info("Closed {} connections. Pool is now empty", closedCount);
    }

    private boolean closeConnectionSilently(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    logger.debug("Connection closed");
                    return true;
                }
            } catch (SQLException e) {
                logger.warn("Error closing connection: {}", e.getMessage());
                ExceptionHandler.handleSQLException(e, "close connection silently");
            }
        }
        return false;
    }
}
