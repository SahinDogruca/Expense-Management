package com.ozandanis.expense.service;

import com.ozandanis.expense.model.Unit;
import com.ozandanis.expense.repository.ExpenseRequestRepository;
import com.ozandanis.expense.repository.UnitRepository;

import java.math.BigDecimal;
import java.sql.SQLException;

public class BudgetService {

    private final UnitRepository unitRepo = new UnitRepository();
    private final ExpenseRequestRepository reqRepo = new ExpenseRequestRepository();

    /**
     * Birimin toplam bütçe limitini döner.
     */
    public BigDecimal getBudgetLimit(int unitId) throws SQLException {
        return unitRepo.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Birim bulunamadı: " + unitId))
                .getBudgetLimit();
    }

    /**
     * Birimin onaylanmış harcamalarının toplamını döner.
     */
    public BigDecimal getTotalSpent(int unitId) throws SQLException {
        return reqRepo.sumApprovedByUnit(unitId);
    }

    /**
     * Birimin kalan bütçesini (limit - harcanan) döner.
     */
    public BigDecimal getRemainingBudget(int unitId) throws SQLException {
        BigDecimal limit = getBudgetLimit(unitId);
        BigDecimal spent = getTotalSpent(unitId);
        return limit.subtract(spent);
    }
}
