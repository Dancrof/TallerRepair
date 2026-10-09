package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.CashMovement;
import com.tallerrepair.tallerrepair.entity.CashSession;
import com.tallerrepair.tallerrepair.enums.CashMovementType;
import com.tallerrepair.tallerrepair.repository.CashSessionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CashDataService {

    private final CashSessionRepository cashSessionRepository = new CashSessionRepository();

    public void ensureDemoCashSession() {
        if (cashSessionRepository.findBySessionNumber("CAJA-001").isPresent()) {
            return;
        }

        CashSession session = new CashSession();
        session.setSessionNumber("CAJA-001");
        session.setOpeningAmount(new BigDecimal("1200.00"));
        session.setStatus("OPEN");
        session.setOpenedAt(LocalDateTime.now().minusHours(3));

        CashMovement saleMovement = new CashMovement();
        saleMovement.setCashSession(session);
        saleMovement.setMovementType(CashMovementType.SALE);
        saleMovement.setAmount(new BigDecimal("540.00"));
        saleMovement.setDescription("Ventas del día");
        saleMovement.setMovementAt(LocalDateTime.now().minusHours(2));

        CashMovement expenseMovement = new CashMovement();
        expenseMovement.setCashSession(session);
        expenseMovement.setMovementType(CashMovementType.EXPENSE);
        expenseMovement.setAmount(new BigDecimal("120.00"));
        expenseMovement.setDescription("Compra de piezas");
        expenseMovement.setMovementAt(LocalDateTime.now().minusHours(1));

        session.getMovements().add(saleMovement);
        session.getMovements().add(expenseMovement);
        session.setTotalIn(new BigDecimal("540.00"));
        session.setTotalOut(new BigDecimal("120.00"));
        session.recalculateBalance();

        cashSessionRepository.save(session);
    }

    public CashSession getCurrentSession() {
        return cashSessionRepository.findBySessionNumber("CAJA-001").orElseGet(() -> {
            ensureDemoCashSession();
            return cashSessionRepository.findBySessionNumber("CAJA-001").orElse(null);
        });
    }

    public List<CashSession> getAllSessionsWithMovements() {
        return cashSessionRepository.findAllWithMovements();
    }
}
