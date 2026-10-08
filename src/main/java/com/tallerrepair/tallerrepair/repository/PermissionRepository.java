package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.Permission;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;

public class PermissionRepository {

    public Optional<Permission> findByCode(String code) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            Permission permission = entityManager
                    .createQuery("SELECT p FROM Permission p WHERE p.code = :code", Permission.class)
                    .setParameter("code", code)
                    .getSingleResult();
            return Optional.of(permission);
        } catch (NoResultException exception) {
            return Optional.empty();
        } finally {
            entityManager.close();
        }
    }

    public Permission save(Permission permission) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (permission.getId() == null) {
                entityManager.persist(permission);
            } else {
                permission = entityManager.merge(permission);
            }
            entityManager.getTransaction().commit();
            return permission;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
