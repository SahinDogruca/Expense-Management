package com.ozandanis.expense;

import com.ozandanis.expense.model.*;
import com.ozandanis.expense.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class RepositoryValidation {
    public static void main(String[] args) {
        try {
            // 1. UnitRepository testi
            UnitRepository unitRepo = new UnitRepository();
            Unit unit = new Unit("IT Departmanı", new BigDecimal("50000.00"));
            unitRepo.save(unit);
            System.out.println("✔ Saved Unit: " + unit);
            Optional<Unit> fetchedUnit = unitRepo.findById(unit.getId());
            System.out.println("✔ Fetched Unit: " + fetchedUnit.orElse(null));

            // 2. EmployeeRepository testi
            EmployeeRepository empRepo = new EmployeeRepository();
            Employee employee = new Employee("Ahmet Yılmaz", unit.getId(), null);
            empRepo.save(employee);
            System.out.println("✔ Saved Employee: " + employee);
            Optional<Employee> fetchedEmp = empRepo.findById(employee.getId());
            System.out.println("✔ Fetched Employee: " + fetchedEmp.orElse(null));

            // 3. ExpenseCategoryRepository testi
            ExpenseCategoryRepository catRepo = new ExpenseCategoryRepository();
            ExpenseCategory category = new ExpenseCategory("Seyahat");
            catRepo.save(category);
            System.out.println("✔ Saved Category: " + category);
            Optional<ExpenseCategory> fetchedCat = catRepo.findById(category.getId());
            System.out.println("✔ Fetched Category: " + fetchedCat.orElse(null));

            // 4. ExpenseRequestRepository testi
            ExpenseRequestRepository reqRepo = new ExpenseRequestRepository();
            ExpenseRequest request = new ExpenseRequest(
                    0,
                    employee.getId(),
                    category.getId(),
                    new BigDecimal("123.45"),
                    LocalDate.now(),
                    "PENDING",
                    null
            );
            reqRepo.save(request);
            System.out.println("✔ Saved ExpenseRequest: " + request);
            // Durum güncelleme
            reqRepo.updateStatus(request.getId(), "APPROVED");
            Optional<ExpenseRequest> fetchedReq = reqRepo.findById(request.getId());
            System.out.println("✔ Updated ExpenseRequest: " + fetchedReq.orElse(null));

            // 5. ReimbursementRepository testi
            ReimbursementRepository reimbRepo = new ReimbursementRepository();
            Reimbursement reimb = new Reimbursement(
                    0,
                    request.getId(),
                    request.getAmount(),
                    LocalDate.now()
            );
            reimbRepo.save(reimb);
            System.out.println("✔ Saved Reimbursement: " + reimb);
            Optional<Reimbursement> fetchedReimb = reimbRepo.findById(reimb.getId());
            System.out.println("✔ Fetched Reimbursement: " + fetchedReimb.orElse(null));

            System.out.println("\n✅ All repository operations completed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
