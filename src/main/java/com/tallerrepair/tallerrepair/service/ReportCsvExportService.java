package com.tallerrepair.tallerrepair.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ReportCsvExportService {

    public void export(Path destination, List<String> headers, List<List<String>> rows) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
            writeRow(writer, headers);
            for (List<String> row : rows) {
                writeRow(writer, row);
            }
        }
    }

    private void writeRow(BufferedWriter writer, List<String> values) throws IOException {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                writer.write(',');
            }
            String value = values.get(index) == null ? "" : values.get(index);
            writer.write('"');
            writer.write(value.replace("\"", "\"\""));
            writer.write('"');
        }
        writer.newLine();
    }
}