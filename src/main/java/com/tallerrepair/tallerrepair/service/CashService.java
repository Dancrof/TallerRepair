package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.CashMovement;
import com.tallerrepair.tallerrepair.entity.CashSession;
import com.tallerrepair.tallerrepair.enums.CashMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CashService {

    public CashSession openSession(String sessionNumber, BigDecimal openingAmount) {
        if (sessionNumber == null || sessionNumber.isBlank()) {
            throw new IllegalArgumentException("El número de caja es requerido.");
        }

        CashSession session = new CashSession();
        session.setSessionNumber(sessionNumber);
        session.setOpeningAmount(openingAmount == null ? BigDecimal.ZERO : openingAmount);
        session.setStatus("OPEN");
        session.setOpenedAt(LocalDateTime.now());
        session.recalculateBalance();
        return session;
    }

    public CashMovement registerMovement(CashSession session, CashMovementType type, BigDecimal amount, String description) {
        if (session == null) {
            throw new IllegalArgumentException("La sesión de caja es requerida.");
        }
        if (type == null) {
            throw new IllegalArgumentException("El tipo de movimiento es requerido.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto del movimiento no es válido.");
        }

        CashMovement movement = new CashMovement();
        movement.setCashSession(session);
        movement.setMovementType(type);
        movement.setAmount(amount);
        movement.setDescription(description);
        movement.setMovementAt(LocalDateTime.now());

        switch (type) {
            case OPENING -> session.setOpeningAmount(session.getOpeningAmount().add(amount));
            case SALE, REFUND, OTHER -> session.setTotalIn(session.getTotalIn().add(amount));
            case EXPENSE, CLOSING -> session.setTotalOut(session.getTotalOut().add(amount));
            default -> {
            }
        }

        session.getMovements().add(movement);
        session.recalculateBalance();
        return movement;
    }
}
