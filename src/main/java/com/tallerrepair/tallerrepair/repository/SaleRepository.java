package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.Sale;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class SaleRepository {

    public Optional<Sale> findBySaleNumber(String saleNumber) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT s FROM Sale s WHERE s.saleNumber = :saleNumber", Sale.class)
                    .setParameter("saleNumber", saleNumber)
                    .getResultStream()
                    .findFirst();
        } finally {
            entityManager.close();
        }
    }

    public List<Sale> findAll() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT s FROM Sale s ORDER BY s.saleDate DESC", Sale.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public List<Sale> findRecent(int limit) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery(
                            "SELECT DISTINCT s FROM Sale s " +
                                    "LEFT JOIN FETCH s.items si " +
                                    "LEFT JOIN FETCH si.product p " +
                                    "ORDER BY s.saleDate DESC",
                            Sale.class
                    )
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public List<Sale> findAllWithDetails() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery(
                            "SELECT DISTINCT s FROM Sale s "
                                    + "LEFT JOIN FETCH s.items si "
                                    + "LEFT JOIN FETCH si.product "
                                    + "ORDER BY s.saleDate DESC",
                            Sale.class
                    )
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public Sale save(Sale sale) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (sale.getId() == null) {
                entityManager.persist(sale);
            } else {
                sale = entityManager.merge(sale);
            }
            entityManager.getTransaction().commit();
            return sale;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
