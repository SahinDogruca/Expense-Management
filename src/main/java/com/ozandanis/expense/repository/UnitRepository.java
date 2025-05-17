package com.ozandanis.expense.repository;

import com.ozandanis.expense.model.Unit;
import com.ozandanis.expense.util.JdbcUtil;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UnitRepository {


    public List<Unit> getAllUnits() throws SQLException {
        String sql = "SELECT * FROM unit";

        List<Unit> units = new ArrayList<>();

        try (Connection connection = JdbcUtil.getConnection();
        Statement statement = connection.createStatement();) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Unit unit = new Unit();
                unit.setId(resultSet.getInt("id"));
                unit.setName(resultSet.getString("name"));
                unit.setBudgetLimit(resultSet.getBigDecimal("budget_limit"));
                unit.setThresholdLimit(resultSet.getBigDecimal("threshold_limit"));

                units.add(unit);
            }
        }

        return units;
    }

    /** ID’ye göre Unit’ı getirir (threshold_limit dahil). */
    public Optional<Unit> findById(int id) throws SQLException {
        String sql = "SELECT id, name, budget_limit, threshold_limit FROM unit WHERE id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Unit u = new Unit(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getBigDecimal("budget_limit"),
                            rs.getBigDecimal("threshold_limit")
                    );
                    return Optional.of(u);
                }
            }
            return Optional.empty();
        }
    }

    /** Yeni bir Unit kaydeder; üretilen ID’yi nesneye set eder. */
    public void save(Unit unit) throws SQLException {
        String sql = "INSERT INTO unit(name, budget_limit, threshold_limit) VALUES(?, ?, ?)";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, unit.getName());
            ps.setBigDecimal(2, unit.getBudgetLimit());
            ps.setBigDecimal(3, unit.getThresholdLimit());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    unit.setId(keys.getInt(1));
                }
            }
        }
    }

    /** Var olan Unit’i günceller (threshold_limit dahil). */
    public void update(Unit unit) throws SQLException {
        String sql = "UPDATE unit SET name = ?, budget_limit = ?, threshold_limit = ? WHERE id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, unit.getName());
            ps.setBigDecimal(2, unit.getBudgetLimit());
            ps.setBigDecimal(3, unit.getThresholdLimit());
            ps.setInt(4, unit.getId());
            ps.executeUpdate();
        }
    }

    /** Verilen ID’ye sahip Unit kaydını siler. */
    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM unit WHERE id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
