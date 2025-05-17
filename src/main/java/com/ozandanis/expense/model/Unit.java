package com.ozandanis.expense.model;

import java.math.BigDecimal;

public class Unit {
    private int id;
    private String name;
    private BigDecimal budgetLimit;
    private BigDecimal thresholdLimit;    // Yeni eşik limiti alanı

    public Unit() {}



    public Unit(int id, String name, BigDecimal budgetLimit, BigDecimal thresholdLimit) {
        this.id = id;
        this.name = name;
        this.budgetLimit = budgetLimit;
        this.thresholdLimit = thresholdLimit;
    }

    public Unit(String name, BigDecimal budgetLimit, BigDecimal thresholdLimit) {
        this.name = name;
        this.budgetLimit = budgetLimit;
        this.thresholdLimit = thresholdLimit;
    }

    public Unit(String name, BigDecimal budgetLimit) {
        this(name, budgetLimit, BigDecimal.ZERO);
    }


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }
    public void setBudgetLimit(BigDecimal budgetLimit) {
        this.budgetLimit = budgetLimit;
    }
    public BigDecimal getThresholdLimit() {
        return thresholdLimit;
    }
    public void setThresholdLimit(BigDecimal thresholdLimit) {
        this.thresholdLimit = thresholdLimit;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", budgetLimit=" + budgetLimit +
                ", thresholdLimit=" + thresholdLimit +
                '}';
    }
}
