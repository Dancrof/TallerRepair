package com.tallerrepair.tallerrepair.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReportDataSourcesTest {

    @Test
    void shouldLoadRowsForEveryReportCategory() {
        new ProductDataService().ensureDemoInventoryData();
        new SaleDataService().ensureDemoSalesData();
        new BudgetDataService().ensureDemoBudgetData();
        new CashDataService().ensureDemoCashSession();

        assertFalse(new SaleDataService().getAllSales().isEmpty());
        assertFalse(new BudgetDataService().getAllBudgets().isEmpty());
        assertFalse(new ProductDataService().getProducts().isEmpty());
        assertFalse(new CashDataService().getAllSessionsWithMovements().isEmpty());
        assertNotNull(new CashDataService().getAllSessionsWithMovements().get(0).getMovements());
    }
}