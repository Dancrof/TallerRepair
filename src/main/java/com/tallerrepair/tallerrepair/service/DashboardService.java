package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Budget;
import com.tallerrepair.tallerrepair.entity.CashSession;
import com.tallerrepair.tallerrepair.entity.Product;
import com.tallerrepair.tallerrepair.entity.Sale;

import java.math.BigDecimal;
import java.util.List;

public class DashboardService {

    private final ProductDataService productDataService = new ProductDataService();
    private final SaleDataService saleDataService = new SaleDataService();
    private final BudgetDataService budgetDataService = new BudgetDataService();
    private final CashDataService cashDataService = new CashDataService();

    public DashboardSummary getDashboardSummary() {
        List<Product> products = productDataService.getProducts();
        List<Sale> sales = saleDataService.getRecentSales(10);
        List<Budget> budgets = budgetDataService.getRecentBudgets(10);
        CashSession session = cashDataService.getCurrentSession();

        BigDecimal totalSales = sales.stream()
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cashBalance = session == null ? BigDecimal.ZERO : session.getCurrentBalance();

        BigDecimal pendingBudgetBalance = budgets.stream()
                .map(budget -> {
                    BigDecimal payments = budgetDataService.getPaymentsForBudget(budget).stream()
                            .map(payment -> payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return budget.getTotal().subtract(payments);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int lowStockProducts = (int) products.stream().filter(Product::isLowStock).count();

        return new DashboardSummary(
                totalSales,
                cashBalance,
                pendingBudgetBalance,
                lowStockProducts,
                sales.size(),
                budgets.size(),
                products.size()
        );
    }

    public record DashboardSummary(
            BigDecimal totalSales,
            BigDecimal cashBalance,
            BigDecimal pendingBudgetBalance,
            int lowStockProducts,
            int salesCount,
            int budgetCount,
            int productCount
    ) {
    }
}
