package com.ozandanis.expense.model;

import com.ozandanis.expense.repository.EmployeeRepository;
import com.ozandanis.expense.repository.ExpenseCategoryRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class ExpenseRequest {
    private int id;
    private int employeeId;
    private int categoryId;
    private BigDecimal amount;
    private String status;
    private LocalDate created_at;
    private LocalDate updated_at;

    public ExpenseRequest() {}

    public ExpenseRequest(int id, int employeeId, int categoryId,
                          BigDecimal amount, LocalDate created_at,
                          String status, LocalDate updated_at) {
        this.id = id;
        this.employeeId = employeeId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.created_at = created_at;
        this.status = status;
        this.updated_at = updated_at;
    }

    // getter/setter’lar
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getCreated_at() { return created_at; }
    public void setCreated_at(LocalDate created_at) { this.created_at = created_at; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDate updated_at) {
        this.updated_at = updated_at;
    }

    public String getCategoryName() throws SQLException {
        ExpenseCategoryRepository categoryRepository = new ExpenseCategoryRepository();
        Optional<ExpenseCategory> categoryOptional =  categoryRepository.findById(this.categoryId);

        if(categoryOptional.isPresent()) {
            return categoryOptional.get().getName();
        } else {
            return "";
        }
    }

    public String getEmployeeName() throws SQLException {
        EmployeeRepository employeeRepository = new EmployeeRepository();
        Optional<Employee> employeeOptional = employeeRepository.findById(this.employeeId);

        if(employeeOptional.isPresent()) {
            return employeeOptional.get().getName();
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        return "ExpenseRequest{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", categoryId=" + categoryId +
                ", amount=" + amount +
                ", created_at=" + created_at +
                ", status='" + status + '\'' +
                '}';
    }
}
