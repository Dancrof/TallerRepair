package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Product;
import com.tallerrepair.tallerrepair.entity.Sale;
import com.tallerrepair.tallerrepair.entity.SaleItem;
import com.tallerrepair.tallerrepair.enums.InventoryMovementType;
import com.tallerrepair.tallerrepair.enums.SaleStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InventoryAndSalesServiceTest {

    private static void assertMoneyEquals(BigDecimal actual, BigDecimal expected) {
        assertEquals(0, actual.compareTo(expected));
    }

    @Test
    void shouldAdjustStockAndCalculateSaleTotals() {
        Product product = new Product();
        product.setName("Pantalla Samsung S10");
        product.setSku("SAMS-S10-001");
        product.setCostPrice(new BigDecimal("120.00"));
        product.setSalePrice(new BigDecimal("199.90"));
        product.setQuantityOnHand(5);
        product.setMinStock(2);

        InventoryService inventoryService = new InventoryService();
        inventoryService.recordMovement(product, 3, InventoryMovementType.IN, "Compra de proveedor");
        assertEquals(8, product.getQuantityOnHand());

        Sale sale = new Sale();
        sale.setStatus(SaleStatus.OPEN);

        SaleItem item = new SaleItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setUnitPrice(product.getSalePrice());
        item.setSale(sale);
        item.recalculateSubtotal();

        sale.getItems().add(item);
        sale.recalculateTotals();

        assertMoneyEquals(sale.getSubtotal(), new BigDecimal("399.80"));
        assertMoneyEquals(sale.getTotal(), new BigDecimal("399.80"));
        assertMoneyEquals(sale.getBalance(), new BigDecimal("399.80"));

        sale.applyPayment(new BigDecimal("399.80"));
        assertMoneyEquals(sale.getBalance(), BigDecimal.ZERO);

        inventoryService.recordMovement(product, 2, InventoryMovementType.OUT, "Venta de producto");
        assertEquals(6, product.getQuantityOnHand());
    }
}
