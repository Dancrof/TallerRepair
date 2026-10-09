package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.repository.CashSessionRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CashDataServiceTest {

    @Test
    void shouldSeedRealCashSessionData() {
        CashDataService service = new CashDataService();
        CashSessionRepository repository = new CashSessionRepository();

        service.ensureDemoCashSession();

        assertTrue(repository.findBySessionNumber("CAJA-001").isPresent(), "Debe existir una sesión de caja real en SQLite");
    }
}
