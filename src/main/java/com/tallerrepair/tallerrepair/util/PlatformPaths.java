package com.tallerrepair.tallerrepair.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PlatformPaths {
    private static final String APP_FOLDER = ".tallerrepair";

    private PlatformPaths() {
    }

    public static Path appDataDirectory() {
        return ensureDirectory(Path.of(System.getProperty("user.home"), APP_FOLDER));
    }

    public static Path configDirectory() {
        return ensureDirectory(appDataDirectory().resolve("config"));
    }

    public static Path logsDirectory() {
        return ensureDirectory(appDataDirectory().resolve("logs"));
    }

    public static Path photosDirectory() {
        return ensureDirectory(appDataDirectory().resolve("photos"));
    }

    public static Path documentsDirectory() {
        return ensureDirectory(appDataDirectory().resolve("documents"));
    }

    public static Path backupsDirectory() {
        return ensureDirectory(appDataDirectory().resolve("backups"));
    }

    private static Path ensureDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
            return directory;
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo crear el directorio de la aplicación: " + directory, exception);
        }
    }
}
