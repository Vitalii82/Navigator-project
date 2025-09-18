package com.navigator.db.conection;

import java.sql.SQLException;


import com.sun.tools.javac.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ExceptionHandler {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void handleSQLException(SQLException e, String operation) {
        logger.warn("SQL Error during {}: {}", operation, e.getMessage());
    }

    public static void handleException(Exception e, String context) {
        logger.warn("Error in {}: {}", context, e.getMessage());

    }

    public static String getFriendlyErrorMessage(SQLException e) {
        return switch (e.getSQLState()) {
            case "23000" ->
                    "Operation failed: data integrity constraint violated";
            case "08000" ->
                    "Database connection error. Please try again later.";
            case "22003" ->
                    "Invalid numeric value provided";
            default -> "An unexpected database error occurred";
        };
    }
}