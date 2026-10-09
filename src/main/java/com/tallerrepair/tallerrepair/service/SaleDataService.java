package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Product;
import com.tallerrepair.tallerrepair.entity.Sale;
import com.tallerrepair.tallerrepair.entity.SaleItem;
import com.tallerrepair.tallerrepair.enums.SaleStatus;
import com.tallerrepair.tallerrepair.repository.ProductRepository;
import com.tallerrepair.tallerrepair.repository.SaleRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SaleDataService {

    private final SaleRepository saleRepository = new SaleRepository();
    private final ProductRepository productRepository = new ProductRepository();

    public void ensureDemoSalesData() {
        if (!saleRepository.findAll().isEmpty()) {
            return;
        }

        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return;
        }

        Sale sale1 = buildSale("VT-000001", LocalDateTime.now().minusDays(1), SaleStatus.PAID, products.get(0), 1, BigDecimal.ZERO);
        Sale sale2 = buildSale("VT-000002", LocalDateTime.now().minusHours(5), SaleStatus.PAID, products.get(1), 2, new BigDecimal("10.00"));
        Sale sale3 = buildSale("VT-000003", LocalDateTime.now().minusHours(2), SaleStatus.OPEN, products.get(3), 1, BigDecimal.ZERO);

        saleRepository.save(sale1);
        saleRepository.save(sale2);
        saleRepository.save(sale3);
    }

    public List<Sale> getRecentSales(int limit) {
        return saleRepository.findRecent(limit);
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAllWithDetails();
    }

    private Sale buildSale(String saleNumber, LocalDateTime saleDate, SaleStatus status, Product product, int quantity, BigDecimal discount) {
        Sale sale = new Sale();
        sale.setSaleNumber(saleNumber);
        sale.setSaleDate(saleDate);
        sale.setStatus(status);
        sale.setDiscount(discount == null ? BigDecimal.ZERO : discount);

        SaleItem item = new SaleItem();
        item.setSale(sale);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getSalePrice());
        item.recalculateSubtotal();

        sale.getItems().add(item);
        sale.recalculateTotals();

        if (status == SaleStatus.PAID) {
            sale.setPaid(sale.getTotal());
            sale.setBalance(BigDecimal.ZERO);
        } else {
            sale.setPaid(BigDecimal.ZERO);
            sale.setBalance(sale.getTotal());
        }

        return sale;
    }
}
