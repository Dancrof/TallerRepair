package com.tallerrepair.tallerrepair.repository;

import com.tallerrepair.tallerrepair.entity.Quote;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class QuoteRepository {

    public Optional<Quote> findByQuoteNumber(String quoteNumber) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT q FROM Quote q WHERE q.quoteNumber = :quoteNumber", Quote.class)
                    .setParameter("quoteNumber", quoteNumber)
                    .getResultStream()
                    .findFirst();
        } finally {
            entityManager.close();
        }
    }

    public List<Quote> findByServiceOrderId(Long serviceOrderId) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager
                    .createQuery("SELECT q FROM Quote q WHERE q.serviceOrder.id = :serviceOrderId ORDER BY q.createdAt DESC", Quote.class)
                    .setParameter("serviceOrderId", serviceOrderId)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public long countAll() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return entityManager.createQuery("SELECT COUNT(q) FROM Quote q", Long.class).getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    public Quote save(Quote quote) {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (quote.getId() == null) {
                entityManager.persist(quote);
            } else {
                quote = entityManager.merge(quote);
            }
            entityManager.getTransaction().commit();
            return quote;
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
