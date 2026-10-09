package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.TechnicalTest;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TechnicalTestRepository {

    public List<TechnicalTest> findByServiceOrderId(Long serviceOrderId) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT t FROM TechnicalTest t WHERE t.serviceOrder.id = :serviceOrderId ORDER BY t.testedAt DESC", TechnicalTest.class)
                    .setParameter("serviceOrderId", serviceOrderId)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public TechnicalTest save(TechnicalTest test) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (test.getId() == null) {
                entityManager.persist(test);
            } else {
                test = entityManager.merge(test);
            }
            entityManager.getTransaction().commit();
            return test;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
