package com.ozandanis.expense;

import com.ozandanis.expense.model.ExpenseRequest;
import com.ozandanis.expense.model.Unit;
import com.ozandanis.expense.service.BudgetService;
import com.ozandanis.expense.service.ExpenseRequestService;
import com.ozandanis.expense.repository.UnitRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RepositoryAndServiceValidation {
    public static void main(String[] args) {
        try {
            // 1. Yeni bir birim ve harcama talebi oluştur
            UnitRepository unitRepo = new UnitRepository();
            Unit unit = new Unit("Pazarlama", new BigDecimal("20000"));
            unitRepo.save(unit);

            ExpenseRequestService reqService = new ExpenseRequestService();
            ExpenseRequest req = reqService.createRequest(
                    /*employeeId=*/1, /*categoryId=*/1,
                    new BigDecimal("1500"), LocalDate.now()
            );
            System.out.println("✔ Created Request: " + req);

            // 2. Onaylamayı deneriz
            reqService.approve(req.getId());
            System.out.println("✔ Approved Request ID: " + req.getId());

            // 3. Bütçe durumunu kontrol ederiz
            BudgetService budgetService = new BudgetService();
            System.out.println("Limit:      " +
                    budgetService.getBudgetLimit(unit.getId()));
            System.out.println("Harcanan:   " +
                    budgetService.getTotalSpent(unit.getId()));
            System.out.println("Kalan Bütçe: " +
                    budgetService.getRemainingBudget(unit.getId()));

            System.out.println("\n✅ Service katmanı da doğru çalışıyor!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
