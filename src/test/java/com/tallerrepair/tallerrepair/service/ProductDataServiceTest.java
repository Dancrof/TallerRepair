package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductDataServiceTest {

    @Test
    void shouldSeedRealInventoryData() {
        ProductDataService service = new ProductDataService();
        ProductRepository repository = new ProductRepository();

        service.ensureDemoInventoryData();

        assertTrue(repository.findLowStock().size() >= 0, "Debe poder consultar productos de inventario");
        assertTrue(repository.findAll().size() >= 3, "Debe existir inventario real sembrado");
    }
}
