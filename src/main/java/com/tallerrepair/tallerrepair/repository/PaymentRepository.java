package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.Payment;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class PaymentRepository {

    public List<Payment> findByBudgetId(Long budgetId) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT p FROM Payment p WHERE p.budget.id = :budgetId ORDER BY p.receivedAt DESC", Payment.class)
                    .setParameter("budgetId", budgetId)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public List<Payment> findByServiceOrderId(Long serviceOrderId) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT p FROM Payment p WHERE p.serviceOrder.id = :serviceOrderId ORDER BY p.receivedAt DESC", Payment.class)
                    .setParameter("serviceOrderId", serviceOrderId)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public Payment save(Payment payment) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (payment.getId() == null) {
                entityManager.persist(payment);
            } else {
                payment = entityManager.merge(payment);
            }
            entityManager.getTransaction().commit();
            return payment;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
