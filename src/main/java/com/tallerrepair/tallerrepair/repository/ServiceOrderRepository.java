package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.ServiceOrder;
import com.tallerrepair.tallerrepair.entity.Payment;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class ServiceOrderRepository {

    public Optional<ServiceOrder> findByOrderNumber(String orderNumber) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT s FROM ServiceOrder s WHERE s.orderNumber = :orderNumber", ServiceOrder.class)
                    .setParameter("orderNumber", orderNumber)
                    .getResultStream()
                    .findFirst();
        } finally {
            entityManager.close();
        }
    }

    public List<ServiceOrder> findAll() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT s FROM ServiceOrder s ORDER BY s.receivedAt DESC", ServiceOrder.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public List<ServiceOrder> findAllWithRelations() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT s FROM ServiceOrder s "
                            + "JOIN FETCH s.customer "
                            + "JOIN FETCH s.device d "
                            + "JOIN FETCH d.customer "
                            + "LEFT JOIN FETCH s.technician "
                            + "ORDER BY s.receivedAt DESC", ServiceOrder.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public ServiceOrder save(ServiceOrder serviceOrder) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (serviceOrder.getId() == null) {
                entityManager.persist(serviceOrder);
            } else {
                serviceOrder = entityManager.merge(serviceOrder);
            }
            entityManager.getTransaction().commit();
            return serviceOrder;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }

    public ServiceOrder saveWithPayment(ServiceOrder serviceOrder, Payment payment) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(serviceOrder);
            if (payment != null) {
                payment.setServiceOrder(serviceOrder);
                entityManager.persist(payment);
            }
            entityManager.getTransaction().commit();
            return serviceOrder;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
