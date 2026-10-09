package com.tallerrepair.tallerrepair.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportCsvExportServiceTest {

    @Test
    void shouldExportCsvWithEscapedFields() throws Exception {
        Path file = Files.createTempFile("tallerrepair-report-", ".csv");
        try {
            new ReportCsvExportService().export(file,
                    List.of("Orden", "Detalle"),
                    List.of(List.of("OT-001", "Cambio, pieza \"A\"")));
            String csv = Files.readString(file);
            assertTrue(csv.contains("\"Cambio, pieza \"\"A\"\"\""));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}