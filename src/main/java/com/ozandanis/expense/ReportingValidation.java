package com.ozandanis.expense;

import com.ozandanis.expense.service.ReportingService;

public class ReportingValidation {
    public static void main(String[] args) {
        try {
            ReportingService rpt = new ReportingService();

            System.out.println("== Kişi Bazlı Harcama ==");
            rpt.getTotalByEmployee()
                    .forEach((name, total) ->
                            System.out.printf("%s → %.2f₺%n", name, total)
                    );

            System.out.println("\n== Birim/Ay Bazlı Harcama ==");
            rpt.getTotalByUnitPerMonth()
                    .forEach((key, total) ->
                            System.out.printf("%s → %.2f₺%n", key, total)
                    );

            System.out.println("\n== Kategori Bazlı Harcama ==");
            rpt.getTotalByCategory()
                    .forEach((cat, total) ->
                            System.out.printf("%s → %.2f₺%n", cat, total)
                    );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
