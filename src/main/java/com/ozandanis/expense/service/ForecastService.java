package com.ozandanis.expense.service;

import com.ozandanis.expense.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ForecastService {

    /**
     * Geçmiş yılların onaylı harcama toplamlarını listeler.
     */
    public List<Double> getAnnualTotals() throws SQLException {
        String sql =
                "SELECT SUM(amount) AS total " +
                        "FROM expense_request " +
                        "WHERE status = 'APPROVED' " +
                        "GROUP BY DATE_TRUNC('year', date) " +
                        "ORDER BY DATE_TRUNC('year', date)";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Double> totals = new ArrayList<>();
            while (rs.next()) {
                totals.add(rs.getDouble("total"));
            }
            return totals;
        }
    }

    /**
     * Basit hareketli ortalama ile bir sonraki yıl tahmini yapar.
     */
    public double forecastNextYear() throws SQLException {
        List<Double> history = getAnnualTotals();
        return history.stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0);
    }
}
