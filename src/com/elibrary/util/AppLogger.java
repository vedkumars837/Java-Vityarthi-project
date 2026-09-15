package com.elibrary.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Minimal file-based logging utility. Appends timestamped INFO/ERROR
 * lines to logs/application.log so key events and failures are
 * traceable after a run, satisfying the logging/monitoring
 * non-functional requirement.
 */
public final class AppLogger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = LOG_DIR + "/application.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AppLogger() {
        // utility class - no instances
    }

    public static void info(String message) {
        write("INFO", message);
    }

    public static void error(String message) {
        write("ERROR", message);
    }

    private static void write(String level, String message) {
        try {
            java.io.File dir = new java.io.File(LOG_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                writer.println(String.format("[%s] %s - %s", timestamp, level, message));
            }
        } catch (IOException e) {
            // Logging must never crash the application; fall back to stderr.
            System.err.println("Failed to write log entry: " + e.getMessage());
        }
    }
}
