package com.ozandanis.expense.repository;

import com.ozandanis.expense.model.ExpenseCategory;
import com.ozandanis.expense.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExpenseCategoryRepository {

    public Optional<ExpenseCategory> findById(int id) throws SQLException {
        String sql = "SELECT id, name FROM expense_category WHERE id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new ExpenseCategory(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
            return Optional.empty();
        }
    }

    public List<ExpenseCategory> findAll() throws SQLException {
        String sql = "SELECT id, name FROM expense_category";
        List<ExpenseCategory> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new ExpenseCategory(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        }
        return list;
    }

    public void save(ExpenseCategory category) throws SQLException {
        String sql = "INSERT INTO expense_category(name) VALUES(?)";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                category.setId(keys.getInt(1));
            }
        }
    }

    public void update(ExpenseCategory category) throws SQLException {
        String sql = "UPDATE expense_category SET name = ? WHERE id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setInt(2, category.getId());
            ps.executeUpdate();
        }
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM expense_category WHERE id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
