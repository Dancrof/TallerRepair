package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.ServiceOrder;
import org.junit.jupiter.api.Test;

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
}