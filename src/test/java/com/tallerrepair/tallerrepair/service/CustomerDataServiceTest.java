package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Customer;
import com.tallerrepair.tallerrepair.enums.CustomerType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerDataServiceTest {

    @Test
    void shouldCreateEditAndDeactivateCustomer() {
        CustomerDataService service = new CustomerDataService();
        String document = "TEST-" + UUID.randomUUID();

        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.PERSON);
        customer.setFirstName("Cliente");
        customer.setLastName("Prueba");
        customer.setDocument(document);
        customer.setPhone("600123456");

        Customer saved = service.save(customer);
        assertTrue(service.getActiveCustomers().stream().anyMatch(item -> item.getId().equals(saved.getId())));

        saved.setPhone("600654321");
        saved.setCustomerType(CustomerType.COMPANY);
        saved.setBusinessName("Taller de prueba");
        Customer updated = service.save(saved);
        assertTrue("600654321".equals(updated.getPhone()));
        assertTrue(updated.getFirstName() == null && updated.getLastName() == null);

        Customer duplicate = new Customer();
        duplicate.setCustomerType(CustomerType.COMPANY);
        duplicate.setBusinessName("Empresa duplicada");
        duplicate.setDocument(document);
        duplicate.setEmail("contacto@example.test");
        assertThrows(IllegalArgumentException.class, () -> service.save(duplicate));

        service.deactivate(updated);
        assertFalse(service.getActiveCustomers().stream().anyMatch(item -> item.getId().equals(updated.getId())));
    }
}