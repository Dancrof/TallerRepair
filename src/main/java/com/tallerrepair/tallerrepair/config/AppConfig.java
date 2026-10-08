package com.tallerrepair.tallerrepair.config;

import com.tallerrepair.tallerrepair.util.PlatformPaths;

import java.nio.file.Path;

public final class AppConfig {
    public static final String APP_NAME = "TallerRepair";
    public static final String APP_VERSION = "1.0.0";
    public static final String DEFAULT_CURRENCY = "USD";
    public static final Path APP_HOME = PlatformPaths.appDataDirectory();
    public static final Path CONFIG_DIR = PlatformPaths.configDirectory();
    public static final Path LOGS_DIR = PlatformPaths.logsDirectory();
    public static final Path PHOTOS_DIR = PlatformPaths.photosDirectory();
    public static final Path DOCUMENTS_DIR = PlatformPaths.documentsDirectory();
    public static final Path BACKUPS_DIR = PlatformPaths.backupsDirectory();

    private AppConfig() {
    }
}
