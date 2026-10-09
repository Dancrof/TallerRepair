package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.Product;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class ProductRepository {

    public Optional<Product> findBySku(String sku) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT p FROM Product p WHERE p.sku = :sku", Product.class)
                    .setParameter("sku", sku)
                    .getResultStream()
                    .findFirst();
        } finally {
            entityManager.close();
        }
    }

    public List<Product> findAll() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT p FROM Product p ORDER BY p.name ASC", Product.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public List<Product> findLowStock() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT p FROM Product p WHERE p.quantityOnHand <= p.minStock ORDER BY p.name ASC", Product.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public long countAll() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager.createQuery("SELECT COUNT(p) FROM Product p", Long.class).getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    public Product save(Product product) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (product.getId() == null) {
                entityManager.persist(product);
            } else {
                product = entityManager.merge(product);
            }
            entityManager.getTransaction().commit();
            return product;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
