package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.InventoryMovement;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class InventoryMovementRepository {

    public List<InventoryMovement> findByProductId(Long productId) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT i FROM InventoryMovement i WHERE i.product.id = :productId ORDER BY i.movementAt DESC", InventoryMovement.class)
                    .setParameter("productId", productId)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public InventoryMovement save(InventoryMovement movement) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (movement.getId() == null) {
                entityManager.persist(movement);
            } else {
                movement = entityManager.merge(movement);
            }
            entityManager.getTransaction().commit();
            return movement;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
