package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Budget;
import com.tallerrepair.tallerrepair.entity.Payment;
import com.tallerrepair.tallerrepair.entity.Product;
import com.tallerrepair.tallerrepair.entity.ServiceOrder;
import com.tallerrepair.tallerrepair.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OperationalClosureService {

    private final BillingService billingService = new BillingService();

    public OrderClosureResult closeOrder(
            ServiceOrder order,
            BigDecimal partsCost,
            BigDecimal laborCost,
            BigDecimal discount,
            String notes,
            List<ProductUsage> productUsages,
            BigDecimal cashCollection,
            PaymentMethod paymentMethod,
            String referenceNumber
    ) {
        if (order == null) {
            throw new IllegalArgumentException("La orden es requerida para cerrar la operación.");
        }

        BigDecimal safePartsCost = partsCost == null ? BigDecimal.ZERO : partsCost;
        BigDecimal safeLaborCost = laborCost == null ? BigDecimal.ZERO : laborCost;
        BigDecimal safeDiscount = discount == null ? BigDecimal.ZERO : discount;
        BigDecimal safeCashCollection = cashCollection == null ? BigDecimal.ZERO : cashCollection;

        Budget budget = billingService.createBudget(order, safePartsCost, safeLaborCost, safeDiscount, notes);
        order.setTotal(budget.getTotal());

        Payment payment = null;
        if (safeCashCollection.compareTo(BigDecimal.ZERO) > 0) {
            payment = billingService.registerPayment(order, safeCashCollection, paymentMethod, referenceNumber);
            order.setBalance(order.getTotal().subtract(order.getPaid() == null ? BigDecimal.ZERO : order.getPaid()).max(BigDecimal.ZERO));
        }

        List<ProductUsage> stockMovements = new ArrayList<>();
        if (productUsages != null) {
            for (ProductUsage usage : productUsages) {
                if (usage == null || usage.product() == null) {
                    continue;
                }
                Product product = usage.product();
                int quantity = usage.quantity();
                if (quantity <= 0) {
                    continue;
                }
                if (product.getQuantityOnHand() < quantity) {
                    throw new IllegalStateException("No hay suficiente stock para " + product.getName());
                }
                product.adjustStock(-quantity);
                stockMovements.add(new ProductUsage(product, quantity));
            }
        }

        return new OrderClosureResult(budget, payment, safeCashCollection, order, stockMovements);
    }

    public record ProductUsage(Product product, int quantity) {
    }

    public record OrderClosureResult(
            Budget budget,
            Payment payment,
            BigDecimal cashImpact,
            ServiceOrder order,
            List<ProductUsage> stockMovements
    ) {
    }
}
