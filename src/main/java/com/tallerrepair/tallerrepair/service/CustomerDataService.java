package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Customer;
import com.tallerrepair.tallerrepair.enums.CustomerType;
import com.tallerrepair.tallerrepair.repository.CustomerRepository;

import java.time.LocalDateTime;
import java.util.List;

public class CustomerDataService {

    private final CustomerRepository customerRepository = new CustomerRepository();

    public List<Customer> getActiveCustomers() {
        return customerRepository.findAllActive();
    }

    public Customer save(Customer customer) {
        if (customer == null || customer.getCustomerType() == null) {
            throw new IllegalArgumentException("Selecciona el tipo de cliente.");
        }

        if (customer.getCustomerType() == CustomerType.COMPANY) {
            customer.setBusinessName(clean(customer.getBusinessName()));
            if (customer.getBusinessName() == null) {
                throw new IllegalArgumentException("El nombre de la empresa es obligatorio.");
            }
            customer.setFirstName(null);
            customer.setLastName(null);
        } else {
            customer.setFirstName(clean(customer.getFirstName()));
            customer.setLastName(clean(customer.getLastName()));
            if (customer.getFirstName() == null) {
                throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
            }
            customer.setBusinessName(null);
        }

        customer.setDocument(clean(customer.getDocument()));
        customer.setPhone(clean(customer.getPhone()));
        customer.setEmail(clean(customer.getEmail()));
        customer.setAddress(clean(customer.getAddress()));
        customer.setCity(clean(customer.getCity()));
        customer.setProvince(clean(customer.getProvince()));
        customer.setNotes(clean(customer.getNotes()));

        if (customer.getPhone() == null && customer.getEmail() == null) {
            throw new IllegalArgumentException("Indica un teléfono o un correo electrónico de contacto.");
        }

        if (customer.getDocument() != null) {
            customerRepository.findByDocument(customer.getDocument())
                    .filter(existing -> !existing.getId().equals(customer.getId()))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Ya existe un cliente con ese documento.");
                    });
        }

        customer.setActive(true);
        customer.setDeletedAt(null);
        return customerRepository.save(customer);
    }

    public void deactivate(Customer customer) {
        if (customer == null || customer.getId() == null) {
            throw new IllegalArgumentException("Selecciona un cliente válido.");
        }
        customer.setActive(false);
        customer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}