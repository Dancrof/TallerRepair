package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.CashSession;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.Optional;
import java.util.List;

public class CashSessionRepository {

    public Optional<CashSession> findBySessionNumber(String sessionNumber) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT DISTINCT c FROM CashSession c LEFT JOIN FETCH c.movements "
                        + "WHERE c.sessionNumber = :sessionNumber", CashSession.class)
                    .setParameter("sessionNumber", sessionNumber)
                    .getResultStream()
                    .findFirst();
        } finally {
            entityManager.close();
        }
    }

    public List<CashSession> findAllWithMovements() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT DISTINCT c FROM CashSession c LEFT JOIN FETCH c.movements ORDER BY c.openedAt DESC", CashSession.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public CashSession save(CashSession session) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (session.getId() == null) {
                entityManager.persist(session);
            } else {
                session = entityManager.merge(session);
            }
            entityManager.getTransaction().commit();
            return session;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
