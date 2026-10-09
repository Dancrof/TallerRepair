package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.Device;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class DeviceRepository {

    public List<Device> findAllWithCustomer() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT d FROM Device d JOIN FETCH d.customer ORDER BY d.createdAt DESC", Device.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public List<Device> findByCustomerId(Long customerId) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT d FROM Device d WHERE d.customer.id = :customerId ORDER BY d.createdAt DESC", Device.class)
                    .setParameter("customerId", customerId)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public Device save(Device device) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (device.getId() == null) {
                entityManager.persist(device);
            } else {
                device = entityManager.merge(device);
            }
            entityManager.getTransaction().commit();
            return device;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
