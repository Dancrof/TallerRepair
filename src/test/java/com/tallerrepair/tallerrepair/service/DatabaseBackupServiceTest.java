package com.tallerrepair.tallerrepair.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseBackupServiceTest {

    @Test
    void shouldCreateAndRemoveAReadableDatabaseBackup() throws IOException {
        Path backup = new DatabaseBackupService().createBackup();
        try {
            assertTrue(Files.isRegularFile(backup));
            assertTrue(Files.size(backup) > 0);
        } finally {
            Files.deleteIfExists(backup);
        }
    }
}