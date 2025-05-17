package com.ozandanis.expense.service;

import com.ozandanis.expense.model.ExpenseRequest;
import com.ozandanis.expense.model.Reimbursement;
import com.ozandanis.expense.model.Unit;
import com.ozandanis.expense.repository.EmployeeRepository;
import com.ozandanis.expense.repository.ExpenseRequestRepository;
import com.ozandanis.expense.repository.ReimbursementRepository;
import com.ozandanis.expense.repository.UnitRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

public class ExpenseRequestService {

    private final ExpenseRequestRepository reqRepo    = new ExpenseRequestRepository();
    private final EmployeeRepository      empRepo    = new EmployeeRepository();
    private final UnitRepository          unitRepo   = new UnitRepository();
    private final ReimbursementRepository reimbRepo = new ReimbursementRepository();

    /**
     * Yeni bir harcama talebi oluşturur ve DB'ye kaydeder.
     */
    public ExpenseRequest createRequest(int employeeId,
                                        int categoryId,
                                        BigDecimal amount,
                                        LocalDate date) throws SQLException {
        ExpenseRequest req = new ExpenseRequest(
                0,
                employeeId,
                categoryId,
                amount,
                date,
                "PENDING",
                null
        );
        reqRepo.save(req);
        return req;
    }

    /**
     * Talebi onayla, eşik ve limit kontrollerini uygula:
     *  - Eğer toplam + talep ≤ limit → tam onay
     *  - Eğer limit < toplam + talep ≤ limit+threshold → kısmi tazmin
     *  - Aksi halde → reddet
     *
     * @return Güncellenmiş ExpenseRequest nesnesi (status alanı set edilmiş)
     */
    public ExpenseRequest approve(int requestId) throws SQLException {
        ExpenseRequest req = reqRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Talep bulunamadı: " + requestId));

        int unitId = empRepo.findById(req.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Çalışan bulunamadı: " + req.getEmployeeId()))
                .getUnitId();

        Unit unit = unitRepo.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Birim bulunamadı: " + unitId));

        BigDecimal totalSpent   = reqRepo.sumApprovedByUnit(unitId);
        BigDecimal limit        = unit.getBudgetLimit();
        BigDecimal threshold    = unit.getThresholdLimit();
        BigDecimal requestedAmt = req.getAmount();
        BigDecimal projected    = totalSpent.add(requestedAmt);

        if (projected.compareTo(limit) <= 0) {
            // Tam onay
            reqRepo.updateStatus(requestId, "APPROVED");
            req.setStatus("APPROVED");

        } else if (projected.compareTo(limit.add(threshold)) <= 0) {
            // Kısmi tazmin: limiti aşan kısmı tazmin et
            BigDecimal reimbursable = limit.subtract(totalSpent);
            reimbRepo.save(new Reimbursement(
                    0,
                    requestId,
                    reimbursable,
                    LocalDate.now()
            ));
            reqRepo.updateStatus(requestId, "APPROVED");
            req.setStatus("APPROVED");

        } else {
            // Eşik aşıldı → reddet
            reqRepo.updateStatus(requestId, "REJECTED");
            req.setStatus("REJECTED");
            throw new IllegalStateException(
                    "Talep eşik limitini aşıyor. Toplam talep: " + projected +
                            ", Limit+Threshold: " + limit.add(threshold)
            );
        }

        return req;
    }


    /**
     * Talebi REDDİDER (REJECTED) olarak işaretler.
     */
    public ExpenseRequest reject(int requestId) throws SQLException {
        reqRepo.updateStatus(requestId, "REJECTED");
        ExpenseRequest req = reqRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Talep bulunamadı: " + requestId));
        return req;
    }
}
