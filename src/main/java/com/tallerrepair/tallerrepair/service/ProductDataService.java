package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Product;
import com.tallerrepair.tallerrepair.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

public class ProductDataService {

    private final ProductRepository productRepository = new ProductRepository();

    public void ensureDemoInventoryData() {
        if (productRepository.countAll() > 0L) {
            return;
        }

        List<ProductSeed> seeds = List.of(
                new ProductSeed("PANT-S10-001", "Pantalla Samsung S10", "DISPLAY", new BigDecimal("180.00"), new BigDecimal("219.90"), 8, 5),
                new ProductSeed("BAT-XIAOMI-01", "Batería Xiaomi", "BATTERY", new BigDecimal("32.00"), new BigDecimal("49.90"), 3, 5),
                new ProductSeed("CABLE-USB-C", "Cable USB-C", "ACCESSORY", new BigDecimal("6.50"), new BigDecimal("12.50"), 24, 10),
                new ProductSeed("TECLADO-HP-02", "Teclado HP", "ACCESSORY", new BigDecimal("24.00"), new BigDecimal("46.00"), 14, 8),
                new ProductSeed("PORT-CHARGER", "Cargador 20W", "ACCESSORY", new BigDecimal("11.00"), new BigDecimal("19.90"), 12, 6)
        );

        for (ProductSeed seed : seeds) {
            Product product = new Product();
            product.setSku(seed.sku());
            product.setName(seed.name());
            product.setCategory(seed.category());
            product.setCostPrice(seed.costPrice());
            product.setSalePrice(seed.salePrice());
            product.setQuantityOnHand(seed.quantityOnHand());
            product.setMinStock(seed.minStock());
            product.setActive(true);
            productRepository.save(product);
        }
    }

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    private record ProductSeed(
            String sku,
            String name,
            String category,
            BigDecimal costPrice,
            BigDecimal salePrice,
            int quantityOnHand,
            int minStock
    ) {
    }
}
