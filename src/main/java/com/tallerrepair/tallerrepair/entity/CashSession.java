package com.tallerrepair.tallerrepair.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "cash_sessions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cash_sessions_number", columnNames = "session_number")
        }
)
public class CashSession extends BaseEntity {

    @Column(name = "session_number", nullable = false, length = 40)
    private String sessionNumber;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "opening_amount", precision = 12, scale = 2)
    private BigDecimal openingAmount = BigDecimal.ZERO;

    @Column(name = "total_in", precision = 12, scale = 2)
    private BigDecimal totalIn = BigDecimal.ZERO;

    @Column(name = "total_out", precision = 12, scale = 2)
    private BigDecimal totalOut = BigDecimal.ZERO;

    @Column(name = "current_balance", precision = 12, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "OPEN";

    @OneToMany(mappedBy = "cashSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CashMovement> movements = new ArrayList<>();

    public void recalculateBalance() {
        this.currentBalance = openingAmount
                .add(totalIn)
                .subtract(totalOut);
    }

    public String getSessionNumber() {
        return sessionNumber;
    }

    public void setSessionNumber(String sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public BigDecimal getOpeningAmount() {
        return openingAmount;
    }

    public void setOpeningAmount(BigDecimal openingAmount) {
        this.openingAmount = openingAmount == null ? BigDecimal.ZERO : openingAmount;
        recalculateBalance();
    }

    public BigDecimal getTotalIn() {
        return totalIn;
    }

    public void setTotalIn(BigDecimal totalIn) {
        this.totalIn = totalIn == null ? BigDecimal.ZERO : totalIn;
        recalculateBalance();
    }

    public BigDecimal getTotalOut() {
        return totalOut;
    }

    public void setTotalOut(BigDecimal totalOut) {
        this.totalOut = totalOut == null ? BigDecimal.ZERO : totalOut;
        recalculateBalance();
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance == null ? BigDecimal.ZERO : currentBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CashMovement> getMovements() {
        return movements;
    }

    public void setMovements(List<CashMovement> movements) {
        this.movements = movements == null ? new ArrayList<>() : movements;
    }
}
