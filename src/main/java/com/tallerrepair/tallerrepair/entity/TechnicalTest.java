package com.tallerrepair.tallerrepair.entity;

import com.tallerrepair.tallerrepair.enums.TechnicalTestResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "technical_tests")
public class TechnicalTest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrder serviceOrder;

    @Column(name = "test_name", nullable = false, length = 100)
    private String testName;

    @Column(name = "category", length = 80)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_result", length = 30)
    private TechnicalTestResult entryResult = TechnicalTestResult.NOT_TESTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "exit_result", length = 30)
    private TechnicalTestResult exitResult = TechnicalTestResult.NOT_TESTED;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "tested_at")
    private LocalDateTime testedAt;

    public ServiceOrder getServiceOrder() {
        return serviceOrder;
    }

    public void setServiceOrder(ServiceOrder serviceOrder) {
        this.serviceOrder = serviceOrder;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TechnicalTestResult getEntryResult() {
        return entryResult;
    }

    public void setEntryResult(TechnicalTestResult entryResult) {
        this.entryResult = entryResult;
    }

    public TechnicalTestResult getExitResult() {
        return exitResult;
    }

    public void setExitResult(TechnicalTestResult exitResult) {
        this.exitResult = exitResult;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getTestedAt() {
        return testedAt;
    }

    public void setTestedAt(LocalDateTime testedAt) {
        this.testedAt = testedAt;
    }
}
