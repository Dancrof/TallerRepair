package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Budget;
import com.tallerrepair.tallerrepair.entity.ServiceOrder;
import com.tallerrepair.tallerrepair.enums.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BillingServiceTest {

    @Test
    void shouldCreateBudgetWithCalculatedTotal() {
        ServiceOrder order = new ServiceOrder();
        order.setEstimatedCost(new BigDecimal("150.00"));

        BillingService billingService = new BillingService();
        Budget budget = billingService.createBudget(order, new BigDecimal("80.00"), new BigDecimal("70.00"), new BigDecimal("10.00"), "Revisión de batería y pantalla");

        assertNotNull(budget);
        assertTrue(budget.getBudgetNumber().matches("BUD-\\d{6}"), "Debe generar un número de presupuesto en formato BUD-000000");
        assertEquals(new BigDecimal("140.00"), budget.getTotal());
    }

    @Test
    void shouldRegisterPaymentAndUpdateOrderBalance() {
        ServiceOrder order = new ServiceOrder();
        order.setTotal(new BigDecimal("100.00"));
        order.setPaid(new BigDecimal("0.00"));
        order.setBalance(new BigDecimal("100.00"));

        BillingService billingService = new BillingService();
        billingService.registerPayment(order, new BigDecimal("70.00"), PaymentMethod.CASH, "REF-001");

        assertEquals(new BigDecimal("70.00"), order.getPaid());
        assertEquals(new BigDecimal("30.00"), order.getBalance());
    }
}
