package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.Customer;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

public class CustomerRepository {

    public Optional<Customer> findByDocument(String document) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            Customer customer = entityManager
                    .createQuery("SELECT c FROM Customer c WHERE c.document = :document", Customer.class)
                    .setParameter("document", document)
                    .getSingleResult();
            return Optional.of(customer);
        } catch (NoResultException exception) {
            return Optional.empty();
        } finally {
            entityManager.close();
        }
    }

    public List<Customer> findAllActive() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT c FROM Customer c WHERE c.active = true ORDER BY c.createdAt DESC", Customer.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public Customer save(Customer customer) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (customer.getId() == null) {
                entityManager.persist(customer);
            } else {
                customer = entityManager.merge(customer);
            }
            entityManager.getTransaction().commit();
            return customer;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
