package com.ozandanis.expense.util;

import java.math.BigDecimal;

public class StatisticItem {
    private String name;
    private BigDecimal totalAmount;
    private int count;

    // Constructor
    public StatisticItem(String name, BigDecimal totalAmount, int count) {
        this.name = name;
        this.totalAmount = totalAmount;
        this.count = count;
    }

    // Getters
    public String getName() { return name; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public int getCount() { return count; }
}