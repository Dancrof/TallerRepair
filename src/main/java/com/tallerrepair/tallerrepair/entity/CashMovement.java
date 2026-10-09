package com.tallerrepair.tallerrepair.entity;

import com.tallerrepair.tallerrepair.enums.CashMovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_movements")
public class CashMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_session_id", nullable = false)
    private CashSession cashSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private CashMovementType movementType;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "description", length = 150)
    private String description;

    @Column(name = "movement_at", nullable = false)
    private LocalDateTime movementAt = LocalDateTime.now();

    public CashSession getCashSession() {
        return cashSession;
    }

    public void setCashSession(CashSession cashSession) {
        this.cashSession = cashSession;
    }

    public CashMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(CashMovementType movementType) {
        this.movementType = movementType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount == null ? BigDecimal.ZERO : amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getMovementAt() {
        return movementAt;
    }

    public void setMovementAt(LocalDateTime movementAt) {
        this.movementAt = movementAt;
    }
}
