package com.tallerrepair.tallerrepair.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardServiceTest {

    @Test
    void shouldBuildDashboardSummaryFromRealData() {
        new BudgetDataService().ensureDemoBudgetData();
        new ProductDataService().ensureDemoInventoryData();
        new SaleDataService().ensureDemoSalesData();
        new CashDataService().ensureDemoCashSession();

        DashboardService dashboardService = new DashboardService();
        DashboardService.DashboardSummary summary = dashboardService.getDashboardSummary();

        assertNotNull(summary);
        assertTrue(summary.totalSales().compareTo(BigDecimal.ZERO) >= 0, "Debe devolver ventas totales válidas");
        assertTrue(summary.cashBalance().compareTo(BigDecimal.ZERO) >= 0, "Debe devolver saldo de caja válido");
        assertTrue(summary.lowStockProducts() >= 0, "Debe devolver un número válido de productos con stock bajo");
    }
}
