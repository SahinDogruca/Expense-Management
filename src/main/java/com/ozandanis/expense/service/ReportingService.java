package com.ozandanis.expense.service;

import com.ozandanis.expense.util.JdbcUtil;
import com.ozandanis.expense.util.StatisticItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportingService {

    /**
     * Kişi bazında onaylı harcamaların toplamını döner.
     */
    public Map<String, Double> getTotalByEmployee() throws SQLException {
        String sql =
                "SELECT e.name AS employee_name, SUM(er.amount) AS total_spent " +
                        "FROM expense_request er " +
                        "JOIN employee e ON er.employee_id = e.id " +
                        "WHERE er.status = 'APPROVED' " +
                        "GROUP BY e.name";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Map<String, Double> report = new HashMap<>();
            while (rs.next()) {
                report.put(
                        rs.getString("employee_name"),
                        rs.getDouble("total_spent")
                );
            }
            return report;
        }
    }

    /**
     * Birim bazında aylık onaylı harcamaların toplamını döner.
     */
    public Map<String, Double> getTotalByUnitPerMonth() throws SQLException {
        String sql =
                "SELECT u.name AS unit_name, DATE_TRUNC('month', er.date) AS mon, SUM(er.amount) AS total_spent " +
                        "FROM expense_request er " +
                        "JOIN employee e ON er.employee_id = e.id " +
                        "JOIN unit u ON e.unit_id = u.id " +
                        "WHERE er.status = 'APPROVED' " +
                        "GROUP BY u.name, mon " +
                        "ORDER BY mon, u.name";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Map<String, Double> report = new HashMap<>();
            while (rs.next()) {
                String key = rs.getString("unit_name")
                        + " / " + rs.getDate("mon").toLocalDate().getMonth();
                report.put(
                        key,
                        rs.getDouble("total_spent")
                );
            }
            return report;
        }
    }

    /**
     * Kategori bazında onaylı harcamaların toplamını döner.
     */
    public Map<String, Double> getTotalByCategory() throws SQLException {
        String sql =
                "SELECT c.name AS category_name, SUM(er.amount) AS total_spent " +
                        "FROM expense_request er " +
                        "JOIN expense_category c ON er.category_id = c.id " +
                        "WHERE er.status = 'APPROVED' " +
                        "GROUP BY c.name";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Map<String, Double> report = new HashMap<>();
            while (rs.next()) {
                report.put(
                        rs.getString("category_name"),
                        rs.getDouble("total_spent")
                );
            }
            return report;
        }
    }

    public List<StatisticItem> getExpensesByEmployee() throws SQLException {
        List<StatisticItem> stats = new ArrayList<>();
        String sql = "SELECT e.name, SUM(er.amount) as total_amount, COUNT(er.id) as count " +
                "FROM expense_request er " +
                "JOIN employee e ON er.employee_id = e.id " +
                "GROUP BY e.name";

        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stats.add(new StatisticItem(
                        rs.getString("name"),
                        rs.getBigDecimal("total_amount"),
                        rs.getInt("count")
                ));
            }
        }
        return stats;
    }


    public List<StatisticItem> getExpensesByUnit() throws SQLException {
        List<StatisticItem> stats = new ArrayList<>();
        String sql = "SELECT u.name, SUM(er.amount) as total_amount, COUNT(er.id) as count " +
                "FROM expense_request er " +
                "JOIN employee e ON er.employee_id = e.id " +
                "JOIN unit u ON e.unit_id = u.id " +
                "GROUP BY u.name";

        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stats.add(new StatisticItem(
                        rs.getString("name"),
                        rs.getBigDecimal("total_amount"),
                        rs.getInt("count")
                ));
            }
        }
        return stats;
    }


    public List<StatisticItem> getExpensesByCategory() throws SQLException {
        List<StatisticItem> stats = new ArrayList<>();
        String sql = "SELECT ec.name, SUM(er.amount) as total_amount, COUNT(er.id) as count " +
                "FROM expense_request er " +
                "JOIN expense_category ec ON er.category_id = ec.id " +
                "GROUP BY ec.name";

        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stats.add(new StatisticItem(
                        rs.getString("name"),
                        rs.getBigDecimal("total_amount"),
                        rs.getInt("count")
                ));
            }
        }
        return stats;
    }


    public List<StatisticItem> getMonthlyExpenses() throws SQLException {
        List<StatisticItem> stats = new ArrayList<>();
        String sql = "SELECT TO_CHAR(er.created_at, 'YYYY-MM') as month, " +
                "SUM(er.amount) as total_amount, COUNT(er.id) as count " +
                "FROM expense_request er " +
                "GROUP BY TO_CHAR(er.created_at, 'YYYY-MM') " +
                "ORDER BY month";

        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stats.add(new StatisticItem(
                        rs.getString("month"),
                        rs.getBigDecimal("total_amount"),
                        rs.getInt("count")
                ));
            }
        }
        return stats;
    }
}
