package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Device;
import com.tallerrepair.tallerrepair.entity.Payment;
import com.tallerrepair.tallerrepair.entity.ServiceOrder;
import com.tallerrepair.tallerrepair.enums.PaymentMethod;
import com.tallerrepair.tallerrepair.enums.ServiceOrderPriority;
import com.tallerrepair.tallerrepair.enums.ServiceOrderStatus;
import com.tallerrepair.tallerrepair.repository.ServiceOrderRepository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

public class ServiceOrderDataService {

    private final ServiceOrderRepository repository = new ServiceOrderRepository();
    private final ServiceOrderService serviceOrderService = new ServiceOrderService();

    public List<ServiceOrder> getOrders() {
        return repository.findAllWithRelations();
    }

    public ServiceOrder create(ServiceOrder order) {
        return create(order, BigDecimal.ZERO, PaymentMethod.CASH, null);
    }

    public ServiceOrder create(ServiceOrder order, BigDecimal initialPayment, PaymentMethod paymentMethod,
                               String paymentReference) {
        if (order == null || order.getCustomer() == null || order.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Selecciona un cliente.");
        }
        Device device = order.getDevice();
        if (device == null || device.getId() == null) {
            throw new IllegalArgumentException("Selecciona un equipo.");
        }
        if (!order.getCustomer().getId().equals(device.getCustomer().getId())) {
            throw new IllegalArgumentException("El equipo seleccionado no pertenece al cliente.");
        }
        if (order.getDeclaredFailure() == null || order.getDeclaredFailure().isBlank()) {
            throw new IllegalArgumentException("Describe el problema indicado por el cliente.");
        }
        if (order.getEstimatedCost() != null && order.getEstimatedCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo estimado no puede ser negativo.");
        }
        if (order.getEstimatedDeliveryAt() != null && order.getEstimatedDeliveryAt().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La entrega estimada no puede ser anterior a hoy.");
        }
        BigDecimal safeInitialPayment = initialPayment == null ? BigDecimal.ZERO : initialPayment;
        if (safeInitialPayment.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El anticipo no puede ser negativo.");
        }
        order.setDeclaredFailure(order.getDeclaredFailure().trim());
        order.setIntakeCondition(clean(order.getIntakeCondition()));
        order.setIntakeAccessories(clean(order.getIntakeAccessories()));
        order.setDiagnosis(clean(order.getDiagnosis()));
        order.setWorkPerformed(clean(order.getWorkPerformed()));
        order.setObservations(clean(order.getObservations()));
        if (order.getNotes() != null) {
            order.setNotes(order.getNotes().isBlank() ? null : order.getNotes().trim());
        }

        long nextNumber = getOrders().stream()
                .map(ServiceOrder::getOrderNumber)
                .mapToLong(this::extractSequence)
                .max()
                .orElse(0L) + 1L;
        String orderNumber = serviceOrderService.generateOrderNumber(nextNumber);
        while (repository.findByOrderNumber(orderNumber).isPresent()) {
            orderNumber = serviceOrderService.generateOrderNumber(++nextNumber);
        }

        order.setOrderNumber(orderNumber);
        if (order.getReceivedAt() == null) {
            order.setReceivedAt(LocalDateTime.now());
        }
        order.setStatus(ServiceOrderStatus.RECEIVED);
        if (order.getPriority() == null) {
            order.setPriority(ServiceOrderPriority.NORMAL);
        }
        order.setPaid(safeInitialPayment);
        serviceOrderService.updateTotals(order);
        if (safeInitialPayment.compareTo(order.getTotal()) > 0) {
            throw new IllegalArgumentException("El anticipo no puede superar el total de la orden.");
        }

        Payment payment = null;
        if (safeInitialPayment.compareTo(BigDecimal.ZERO) > 0) {
            payment = new Payment();
            payment.setAmount(safeInitialPayment);
            payment.setPaymentMethod(paymentMethod == null ? PaymentMethod.CASH : paymentMethod);
            payment.setReferenceNumber(clean(paymentReference));
            payment.setReceivedAt(LocalDateTime.now());
        }
        return repository.saveWithPayment(order, payment);
    }

    public void changeStatus(ServiceOrder order, ServiceOrderStatus status, String notes) {
        serviceOrderService.changeStatus(order, status, null, notes);
    }

    private long extractSequence(String orderNumber) {
        if (orderNumber == null) {
            return 0L;
        }
        int separator = orderNumber.lastIndexOf('-');
        if (separator < 0 || separator == orderNumber.length() - 1) {
            return 0L;
        }
        try {
            return Long.parseLong(orderNumber.substring(separator + 1));
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}