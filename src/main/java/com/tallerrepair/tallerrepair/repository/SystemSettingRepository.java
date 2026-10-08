package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.SystemSetting;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;

public class SystemSettingRepository {

    public Optional<SystemSetting> findByKey(String key) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            SystemSetting entity = entityManager
                    .createQuery("SELECT s FROM SystemSetting s WHERE s.key = :key", SystemSetting.class)
                    .setParameter("key", key)
                    .getSingleResult();
            return Optional.of(entity);
        } catch (NoResultException exception) {
            return Optional.empty();
        } finally {
            entityManager.close();
        }
    }

    public SystemSetting save(SystemSetting entity) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (entity.getId() == null) {
                entityManager.persist(entity);
            } else {
                entity = entityManager.merge(entity);
            }
            entityManager.getTransaction().commit();
            return entity;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
