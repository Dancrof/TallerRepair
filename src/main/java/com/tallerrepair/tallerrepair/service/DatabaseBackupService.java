package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.config.AppConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseBackupService {

    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    public Path createBackup() {
        Path databaseFile = AppConfig.APP_HOME.resolve("tallerrepair.db");
        Path backupFile = AppConfig.BACKUPS_DIR.resolve(
                "tallerrepair-" + LocalDateTime.now().format(BACKUP_TIMESTAMP) + ".db");
        try {
            if (!Files.isRegularFile(databaseFile)) {
                throw new IllegalStateException("No se encontró la base de datos: " + databaseFile);
            }
            Files.createDirectories(AppConfig.BACKUPS_DIR);
            try (var connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
                 Statement statement = connection.createStatement()) {
                String target = backupFile.toAbsolutePath().toString().replace("'", "''");
                statement.execute("VACUUM INTO '" + target + "'");
            }
            return backupFile;
        } catch (IOException | SQLException exception) {
            throw new IllegalStateException("No se pudo crear la copia de seguridad.", exception);
        }
    }
}