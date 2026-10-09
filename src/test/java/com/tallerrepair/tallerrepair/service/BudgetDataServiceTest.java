package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.repository.BudgetRepository;
import com.tallerrepair.tallerrepair.repository.PaymentRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BudgetDataServiceTest {

    @Test
    void shouldSeedRealBudgetAndPaymentData() {
        BudgetDataService service = new BudgetDataService();
        BudgetRepository budgetRepository = new BudgetRepository();
        PaymentRepository paymentRepository = new PaymentRepository();

        service.ensureDemoBudgetData();

        assertTrue(budgetRepository.countAll() > 0L, "Debe existir al menos un presupuesto real en SQLite");
        assertTrue(paymentRepository.findByBudgetId(budgetRepository.findRecent(1).get(0).getId()).size() >= 1,
                "Debe existir al menos un pago asociado al presupuesto sembrado");
    }
}
