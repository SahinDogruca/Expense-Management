package com.ozandanis.expense;

import com.ozandanis.expense.model.Unit;
import com.ozandanis.expense.repository.UnitRepository;

import java.math.BigDecimal;
import java.util.Optional;

public class ThresholdValidation {
    public static void main(String[] args) {
        try {
            UnitRepository repo = new UnitRepository();

            // 3 argümanlı constructor kullanın:
            Unit u = new Unit(
                    "Test Birim",                    // name
                    new BigDecimal("10000.00"),      // budgetLimit
                    new BigDecimal("500.00")         // thresholdLimit
            );
            repo.save(u);
            System.out.println("✔ Saved Unit: " + u);

            Optional<Unit> fetched = repo.findById(u.getId());
            System.out.println("✔ Fetched Unit: " + fetched.orElseThrow());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
