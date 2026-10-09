package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.ServiceOrderStatusHistory;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;

public class ServiceOrderStatusHistoryRepository {

    public ServiceOrderStatusHistory save(ServiceOrderStatusHistory history) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (history.getId() == null) {
                entityManager.persist(history);
            } else {
                history = entityManager.merge(history);
            }
            entityManager.getTransaction().commit();
            return history;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
