package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Quote;
import com.tallerrepair.tallerrepair.entity.ServiceOrder;
import com.tallerrepair.tallerrepair.enums.ServiceOrderStatus;
import com.tallerrepair.tallerrepair.repository.QuoteRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class QuoteService {

    private final QuoteRepository quoteRepository = new QuoteRepository();

    public Quote createQuote(ServiceOrder order, BigDecimal subtotal, BigDecimal discount, BigDecimal surcharge) {
        if (order == null) {
            throw new IllegalArgumentException("La orden es requerida para crear un presupuesto.");
        }

        Quote quote = new Quote();
        quote.setServiceOrder(order);
        quote.setQuoteNumber(generateNextQuoteNumber());
        quote.setSubtotal(subtotal == null ? BigDecimal.ZERO : subtotal);
        quote.setDiscount(discount == null ? BigDecimal.ZERO : discount);
        quote.setSurcharge(surcharge == null ? BigDecimal.ZERO : surcharge);
        quote.setAdvancePayment(BigDecimal.ZERO);
        quote.setStatus(ServiceOrderStatus.QUOTED);
        quote.recalculateTotals();

        if (order.getId() != null) {
            return quoteRepository.save(quote);
        }
        return quote;
    }

    public Quote approveQuote(Quote quote) {
        if (quote == null) {
            throw new IllegalArgumentException("La cotización es requerida.");
        }

        quote.setApproved(true);
        quote.setApprovedAt(LocalDateTime.now());
        quote.setStatus(ServiceOrderStatus.APPROVED);

        ServiceOrder order = quote.getServiceOrder();
        if (order != null) {
            order.setStatus(ServiceOrderStatus.APPROVED);
            order.setTotal(quote.getTotal());
            order.setBalance(quote.getBalance());
        }

        return quoteRepository.save(quote);
    }

    public Quote rejectQuote(Quote quote, String reason) {
        if (quote == null) {
            throw new IllegalArgumentException("La cotización es requerida.");
        }

        quote.setApproved(false);
        quote.setApprovedAt(null);
        quote.setStatus(ServiceOrderStatus.QUOTED);

        if (quote.getServiceOrder() != null && quote.getServiceOrder().getStatus() == ServiceOrderStatus.APPROVED) {
            quote.getServiceOrder().setStatus(ServiceOrderStatus.QUOTED);
        }

        return quoteRepository.save(quote);
    }

    public Quote registerAdvancePayment(Quote quote, BigDecimal advancePayment) {
        if (quote == null) {
            throw new IllegalArgumentException("La cotización es requerida.");
        }
        if (advancePayment == null || advancePayment.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El anticipo debe ser mayor o igual a cero.");
        }

        BigDecimal safeAdvance = advancePayment.min(quote.getTotal() == null ? BigDecimal.ZERO : quote.getTotal());
        quote.setAdvancePayment(safeAdvance);
        quote.recalculateTotals();

        if (quote.getServiceOrder() != null) {
            quote.getServiceOrder().setPaid(safeAdvance);
            quote.getServiceOrder().setBalance(
                    quote.getServiceOrder().getTotal() == null ? BigDecimal.ZERO :
                            quote.getServiceOrder().getTotal().subtract(safeAdvance).max(BigDecimal.ZERO)
            );
        }

        return quoteRepository.save(quote);
    }

    public String generateNextQuoteNumber() {
        long count = quoteRepository.countAll();
        return "COT-" + String.format("%06d", count + 1);
    }
}
