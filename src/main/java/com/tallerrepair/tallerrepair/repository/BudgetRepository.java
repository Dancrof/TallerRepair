package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.Budget;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class BudgetRepository {

    public Optional<Budget> findByBudgetNumber(String budgetNumber) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT b FROM Budget b WHERE b.budgetNumber = :budgetNumber", Budget.class)
                    .setParameter("budgetNumber", budgetNumber)
                    .getResultStream()
                    .findFirst();
        } finally {
            entityManager.close();
        }
    }

    public List<Budget> findByServiceOrderId(Long serviceOrderId) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT b FROM Budget b WHERE b.serviceOrder.id = :serviceOrderId ORDER BY b.issuedAt DESC", Budget.class)
                    .setParameter("serviceOrderId", serviceOrderId)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public List<Budget> findRecent(int limit) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery(
                            "SELECT DISTINCT b FROM Budget b " +
                                    "JOIN FETCH b.serviceOrder s " +
                                    "JOIN FETCH s.customer c " +
                                    "JOIN FETCH s.device d " +
                                    "LEFT JOIN FETCH b.payments p " +
                                    "ORDER BY b.issuedAt DESC",
                            Budget.class
                    )
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public List<Budget> findAllWithDetails() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery(
                            "SELECT DISTINCT b FROM Budget b "
                                    + "JOIN FETCH b.serviceOrder s "
                                    + "JOIN FETCH s.customer "
                                    + "JOIN FETCH s.device d "
                                    + "LEFT JOIN FETCH b.payments "
                                    + "ORDER BY b.issuedAt DESC",
                            Budget.class
                    )
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public long countAll() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager.createQuery("SELECT COUNT(b) FROM Budget b", Long.class).getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    public Budget save(Budget budget) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (budget.getId() == null) {
                entityManager.persist(budget);
            } else {
                budget = entityManager.merge(budget);
            }
            entityManager.getTransaction().commit();
            return budget;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
