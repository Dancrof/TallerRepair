package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Product;
import com.tallerrepair.tallerrepair.entity.Sale;
import com.tallerrepair.tallerrepair.entity.SaleItem;
import com.tallerrepair.tallerrepair.enums.SaleStatus;

import java.math.BigDecimal;

public class SaleService {

    public Sale createSale(String saleNumber) {
        Sale sale = new Sale();
        sale.setSaleNumber(saleNumber);
        sale.setStatus(SaleStatus.OPEN);
        return sale;
    }

    public Sale addItem(Sale sale, Product product, int quantity) {
        if (sale == null || product == null) {
            throw new IllegalArgumentException("La venta y el producto son requeridos.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        SaleItem item = new SaleItem();
        item.setSale(sale);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getSalePrice());
        item.recalculateSubtotal();

        sale.getItems().add(item);
        sale.setSubtotal(sale.getItems().stream()
                .map(SaleItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        sale.recalculateTotals();
        return sale;
    }
}
