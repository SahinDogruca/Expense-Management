package com.ozandanis.expense.model;

public class ExpenseCategory {
    private int id;
    private String name;

    public ExpenseCategory() {}

    public ExpenseCategory(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public ExpenseCategory(String name) {
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "ExpenseCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
