package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Budget;
import com.tallerrepair.tallerrepair.entity.Payment;
import com.tallerrepair.tallerrepair.entity.ServiceOrder;
import com.tallerrepair.tallerrepair.enums.BudgetStatus;
import com.tallerrepair.tallerrepair.enums.PaymentMethod;
import com.tallerrepair.tallerrepair.repository.BudgetRepository;
import com.tallerrepair.tallerrepair.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillingService {

    private final BudgetRepository budgetRepository = new BudgetRepository();
    private final PaymentRepository paymentRepository = new PaymentRepository();

    public Budget createBudget(ServiceOrder order, BigDecimal partsCost, BigDecimal laborCost, BigDecimal discount, String notes) {
        if (order == null) {
            throw new IllegalArgumentException("La orden es requerida para crear un presupuesto.");
        }

        BigDecimal safeParts = partsCost == null ? BigDecimal.ZERO : partsCost;
        BigDecimal safeLabor = laborCost == null ? BigDecimal.ZERO : laborCost;
        BigDecimal safeDiscount = discount == null ? BigDecimal.ZERO : discount;

        Budget budget = new Budget();
        budget.setServiceOrder(order);
        budget.setBudgetNumber(generateNextBudgetNumber());
        budget.setPartsCost(safeParts);
        budget.setLaborCost(safeLabor);
        budget.setDiscount(safeDiscount);
        budget.setSurcharge(BigDecimal.ZERO);
        budget.setIssuedAt(LocalDateTime.now());
        budget.setStatus(BudgetStatus.PENDING);
        budget.setNotes(notes);
        budget.recalculateTotal();

        if (order.getId() == null) {
            return budget;
        }

        return budgetRepository.save(budget);
    }

    public Payment registerPayment(ServiceOrder order, BigDecimal amount, PaymentMethod paymentMethod, String referenceNumber) {
        if (order == null) {
            throw new IllegalArgumentException("La orden es requerida para registrar un cobro.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del cobro debe ser mayor que cero.");
        }

        BigDecimal currentPaid = order.getPaid() == null ? BigDecimal.ZERO : order.getPaid();
        BigDecimal currentTotal = order.getTotal() == null ? BigDecimal.ZERO : order.getTotal();

        Payment payment = new Payment();
        payment.setServiceOrder(order);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod == null ? PaymentMethod.CASH : paymentMethod);
        payment.setReferenceNumber(referenceNumber);
        payment.setReceivedAt(LocalDateTime.now());

        order.setPaid(currentPaid.add(amount));
        order.setBalance(currentTotal.subtract(order.getPaid()).max(BigDecimal.ZERO));

        if (order.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            order.setBalance(BigDecimal.ZERO);
        }

        if (order.getId() != null) {
            paymentRepository.save(payment);
        }

        return payment;
    }

    public String generateNextBudgetNumber() {
        long count = budgetRepository.countAll();
        return "BUD-" + String.format("%06d", count + 1);
    }
}
