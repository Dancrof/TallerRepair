package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.ServiceOrder;
import com.tallerrepair.tallerrepair.entity.Device;
import com.tallerrepair.tallerrepair.entity.Payment;
import com.tallerrepair.tallerrepair.enums.PaymentMethod;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceOrderDataServiceTest {

    @Test
    void shouldLoadOrdersWithCustomerAndDeviceAndValidateNewOrder() {
        new BudgetDataService().ensureDemoBudgetData();
        ServiceOrderDataService service = new ServiceOrderDataService();

        var orders = service.getOrders();
        assertFalse(orders.isEmpty());
        assertNotNull(orders.get(0).getCustomer());
        assertNotNull(orders.get(0).getDevice());
        assertNotNull(orders.get(0).getDevice().getCustomer());

        ServiceOrder incompleteOrder = new ServiceOrder();
        incompleteOrder.setCustomer(orders.get(0).getCustomer());
        incompleteOrder.setDevice(orders.get(0).getDevice());
        assertThrows(IllegalArgumentException.class, () -> service.create(incompleteOrder));
    }

    @Test
    void shouldSaveReceptionDetailsAndInitialPaymentAtomically() {
        new BudgetDataService().ensureDemoBudgetData();
        ServiceOrderDataService service = new ServiceOrderDataService();
        Device device = service.getOrders().get(0).getDevice();
        ServiceOrder order = new ServiceOrder();
        order.setCustomer(device.getCustomer());
        order.setDevice(device);
        order.setDeclaredFailure("No carga");
        order.setIntakeCondition("Pantalla con marcas");
        order.setIntakeAccessories("Equipo y cargador");
        order.setObservations("Golpe en esquina");
        order.setEstimatedCost(new BigDecimal("100.00"));
        order.setSurcharge(new BigDecimal("10.00"));
        order.setDiscount(new BigDecimal("5.00"));
        order.setEstimatedDeliveryAt(LocalDate.now().plusDays(3));

        ServiceOrder saved = service.create(order, new BigDecimal("25.00"), PaymentMethod.CARD, "REF-INIT-ORDER");
        try {
            assertEquals(new BigDecimal("105.00"), saved.getTotal());
            assertEquals(new BigDecimal("25.00"), saved.getPaid());
            assertEquals(new BigDecimal("80.00"), saved.getBalance());
            assertEquals("Pantalla con marcas", saved.getIntakeCondition());
            assertEquals("Equipo y cargador", saved.getIntakeAccessories());

            EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
            try {
                List<Payment> payments = entityManager.createQuery(
                                "SELECT p FROM Payment p WHERE p.serviceOrder.id = :id", Payment.class)
                        .setParameter("id", saved.getId()).getResultList();
                assertEquals(1, payments.size());
                assertEquals(PaymentMethod.CARD, payments.get(0).getPaymentMethod());
                assertEquals("REF-INIT-ORDER", payments.get(0).getReferenceNumber());
            } finally {
                entityManager.close();
            }
        } finally {
            EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
            try {
                entityManager.getTransaction().begin();
                entityManager.createQuery("DELETE FROM ServiceOrderStatusHistory h WHERE h.serviceOrder.id = :id")
                        .setParameter("id", saved.getId()).executeUpdate();
                entityManager.createQuery("DELETE FROM Payment p WHERE p.serviceOrder.id = :id")
                        .setParameter("id", saved.getId()).executeUpdate();
                entityManager.createQuery("DELETE FROM ServiceOrder s WHERE s.id = :id")
                        .setParameter("id", saved.getId()).executeUpdate();
                entityManager.getTransaction().commit();
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                entityManager.close();
            }
        }
    }
}