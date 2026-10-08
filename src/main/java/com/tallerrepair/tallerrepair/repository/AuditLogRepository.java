package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.AuditLog;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;

public class AuditLogRepository {

    public void save(AuditLog auditLog) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (auditLog.getId() == null) {
                entityManager.persist(auditLog);
            } else {
                entityManager.merge(auditLog);
            }
            entityManager.getTransaction().commit();
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
