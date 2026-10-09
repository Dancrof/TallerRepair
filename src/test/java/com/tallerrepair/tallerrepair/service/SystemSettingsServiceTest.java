package com.tallerrepair.tallerrepair.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SystemSettingsServiceTest {

    @Test
    void shouldPersistAndRestoreCompanyProfile() {
        SystemSettingsService service = new SystemSettingsService();
        SystemSettingsService.CompanyProfile original = service.getCompanyProfile();
        try {
            service.saveCompanyProfile(new SystemSettingsService.CompanyProfile(
                    "Taller de prueba", "X1234567", "+34 600 111 222", "prueba@example.test", "Calle Uno 1"));
            assertEquals("Taller de prueba", service.getCompanyProfile().name());
            assertThrows(IllegalArgumentException.class, () -> service.saveCompanyProfile(
                    new SystemSettingsService.CompanyProfile(" ", "", "", "", "")));
        } finally {
            service.saveCompanyProfile(original);
        }
    }
}