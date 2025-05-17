package com.ozandanis.expense;

import com.ozandanis.expense.model.*;
import com.ozandanis.expense.repository.*;
import com.ozandanis.expense.service.ExpenseRequestService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ThresholdServiceValidation {
    public static void main(String[] args) {
        try {
            ExpenseRequestService svc = new ExpenseRequestService();

            // --- Senaryo A: Tam Onay ---
            Unit unitA = new Unit("Unit Full", new BigDecimal("1000"), new BigDecimal("200"));
            new UnitRepository().save(unitA);
            Employee empA = new Employee("User A", unitA.getId(), null);
            new EmployeeRepository().save(empA);
            ExpenseCategory catA = new ExpenseCategory("Cat A");
            new ExpenseCategoryRepository().save(catA);

            ExpenseRequest r1 = svc.createRequest(empA.getId(), catA.getId(),
                    new BigDecimal("500"),
                    LocalDate.now());
            r1 = svc.approve(r1.getId());
            System.out.println("R1 Status: " + r1.getStatus());

            // --- Senaryo B: Kısmi Tazmin (limit < tutar ≤ limit+threshold) ---
            Unit unitB = new Unit("Unit Partial", new BigDecimal("1000"), new BigDecimal("200"));
            new UnitRepository().save(unitB);
            Employee empB = new Employee("User B", unitB.getId(), null);
            new EmployeeRepository().save(empB);
            ExpenseCategory catB = new ExpenseCategory("Cat B");
            new ExpenseCategoryRepository().save(catB);

            ExpenseRequest r2 = svc.createRequest(empB.getId(), catB.getId(),
                    new BigDecimal("1100"),
                    LocalDate.now());
            r2 = svc.approve(r2.getId());
            System.out.println("R2 Status: " + r2.getStatus());
            List<Reimbursement> reimbs = new ReimbursementRepository()
                    .findByExpenseId(r2.getId());
            System.out.println("Reimbursement kaydı: " + reimbs);

            // --- Senaryo C: Reddetme (tutar > limit+threshold) ---
            Unit unitC = new Unit("Unit Reject", new BigDecimal("1000"), new BigDecimal("200"));
            new UnitRepository().save(unitC);
            Employee empC = new Employee("User C", unitC.getId(), null);
            new EmployeeRepository().save(empC);
            ExpenseCategory catC = new ExpenseCategory("Cat C");
            new ExpenseCategoryRepository().save(catC);

            ExpenseRequest r3 = svc.createRequest(empC.getId(), catC.getId(),
                    new BigDecimal("1300"),
                    LocalDate.now());
            try {
                r3 = svc.approve(r3.getId());
            } catch (IllegalStateException ex) {
                System.out.println("R3 Exception: " + ex.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
