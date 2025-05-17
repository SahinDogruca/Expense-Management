package com.ozandanis.expense;

import com.ozandanis.expense.service.ForecastService;

public class ForecastValidation {
    public static void main(String[] args) {
        try {
            ForecastService fsvc = new ForecastService();
            System.out.printf("Gelecek Yıl Tahmini: %.2f₺%n",
                    fsvc.forecastNextYear());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
