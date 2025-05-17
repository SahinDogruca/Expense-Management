package com.ozandanis.expense.model;

import com.ozandanis.expense.repository.EmployeeRepository;
import com.ozandanis.expense.repository.ExpenseCategoryRepository;
import com.ozandanis.expense.repository.ExpenseRequestRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class Reimbursement {
    private int id;
    private int expenseId;
    private BigDecimal reimbursedAmount;
    private LocalDate reimbursementDate;

    public Reimbursement() {}

    public Reimbursement(int id, int expenseId,
                         BigDecimal reimbursedAmount,
                         LocalDate reimbursementDate) {
        this.id = id;
        this.expenseId = expenseId;
        this.reimbursedAmount = reimbursedAmount;
        this.reimbursementDate = reimbursementDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }
    public BigDecimal getReimbursedAmount() { return reimbursedAmount; }
    public void setReimbursedAmount(BigDecimal reimbursedAmount) { this.reimbursedAmount = reimbursedAmount; }
    public LocalDate getReimbursementDate() { return reimbursementDate; }
    public void setReimbursementDate(LocalDate reimbursementDate) { this.reimbursementDate = reimbursementDate; }

    public String getCategoryName() throws SQLException {
        ExpenseCategoryRepository expenseCategoryRepository = new ExpenseCategoryRepository();
        ExpenseRequestRepository expenseRequestRepository = new ExpenseRequestRepository();
        Optional<ExpenseRequest> expenseOptional = expenseRequestRepository.findById(this.getExpenseId());

        if(expenseOptional.isPresent()) {
            ExpenseRequest expenseRequest = expenseOptional.get();
            return expenseRequest.getCategoryName();
        } else {
            return "";
        }
    }

    public String getExpenseAmount() throws SQLException {
        ExpenseCategoryRepository expenseCategoryRepository = new ExpenseCategoryRepository();
        ExpenseRequestRepository expenseRequestRepository = new ExpenseRequestRepository();
        Optional<ExpenseRequest> expenseOptional = expenseRequestRepository.findById(this.getExpenseId());
        if(expenseOptional.isPresent()) {
            ExpenseRequest expenseRequest = expenseOptional.get();
            return expenseRequest.getAmount().toString();
        } else {
            return "";
        }
    }

    public String getEmployeeName() throws SQLException {
        EmployeeRepository employeeRepository = new EmployeeRepository();
        Optional<Employee> employeeOptional = employeeRepository.findById(this.getId());

        if(employeeOptional.isPresent()) {
            Employee employee = employeeOptional.get();
            return employee.getName();
        } else {
            return "";
        }
    }


    @Override
    public String toString() {
        return "Reimbursement{" +
                "id=" + id +
                ", expenseId=" + expenseId +
                ", reimbursedAmount=" + reimbursedAmount +
                ", reimbursementDate=" + reimbursementDate +
                '}';
    }
}
