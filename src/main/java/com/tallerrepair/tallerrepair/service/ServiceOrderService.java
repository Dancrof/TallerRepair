package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.ServiceOrder;
import com.tallerrepair.tallerrepair.entity.ServiceOrderStatusHistory;
import com.tallerrepair.tallerrepair.entity.User;
import com.tallerrepair.tallerrepair.enums.ServiceOrderStatus;
import com.tallerrepair.tallerrepair.repository.ServiceOrderRepository;
import com.tallerrepair.tallerrepair.repository.ServiceOrderStatusHistoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public class ServiceOrderService {

    private final ServiceOrderRepository repository = new ServiceOrderRepository();
    private final ServiceOrderStatusHistoryRepository historyRepository = new ServiceOrderStatusHistoryRepository();

    public String generateOrderNumber(Long sequence) {
        return "OT-" + String.format("%06d", sequence);
    }

    public void updateTotals(ServiceOrder order) {
        BigDecimal subtotal = order.getEstimatedCost() == null ? BigDecimal.ZERO : order.getEstimatedCost();
        BigDecimal surcharge = order.getSurcharge() == null ? BigDecimal.ZERO : order.getSurcharge();
        BigDecimal discount = order.getDiscount() == null ? BigDecimal.ZERO : order.getDiscount();

        BigDecimal total = subtotal.add(surcharge).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        order.setTotal(total);
        order.setBalance(total.subtract(order.getPaid() == null ? BigDecimal.ZERO : order.getPaid()));
    }

    public void changeStatus(ServiceOrder order, ServiceOrderStatus newStatus, User user, String notes) {
        if (order == null || newStatus == null) {
            throw new IllegalArgumentException("La orden y el nuevo estado son requeridos.");
        }

        ServiceOrderStatus previous = order.getStatus();
        if (previous == ServiceOrderStatus.DELIVERED && newStatus != ServiceOrderStatus.DELIVERED) {
            throw new IllegalStateException("Una orden entregada no puede volver a un estado anterior.");
        }

        Set<ServiceOrderStatus> allowedTransitions = Set.of(
                ServiceOrderStatus.RECEIVED,
                ServiceOrderStatus.DIAGNOSIS,
                ServiceOrderStatus.QUOTED,
                ServiceOrderStatus.APPROVED,
                ServiceOrderStatus.IN_REPAIR,
                ServiceOrderStatus.WAITING_PARTS,
                ServiceOrderStatus.READY,
                ServiceOrderStatus.DELIVERED,
                ServiceOrderStatus.CANCELLED
        );

        if (!allowedTransitions.contains(newStatus)) {
            throw new IllegalStateException("Estado no válido para la orden.");
        }

        if (previous == ServiceOrderStatus.CANCELLED && newStatus != ServiceOrderStatus.CANCELLED) {
            throw new IllegalStateException("Una orden cancelada no puede reactivarse.");
        }

        order.setStatus(newStatus);
        repository.save(order);

        ServiceOrderStatusHistory history = new ServiceOrderStatusHistory();
        history.setServiceOrder(order);
        history.setPreviousStatus(previous);
        history.setNewStatus(newStatus);
        history.setNotes(notes);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedByUser(user);
        historyRepository.save(history);
    }
}
