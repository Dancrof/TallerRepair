package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.repository.SaleRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SaleDataServiceTest {

    @Test
    void shouldSeedRealSalesData() {
        SaleDataService service = new SaleDataService();
        SaleRepository saleRepository = new SaleRepository();

        service.ensureDemoSalesData();

        assertTrue(saleRepository.findAll().size() >= 1, "Debe existir al menos una venta real en SQLite");
    }
}
