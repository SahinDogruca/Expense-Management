package com.ozandanis.expense.repository;

import com.ozandanis.expense.model.Employee;
import com.ozandanis.expense.model.ExpenseCategory;
import com.ozandanis.expense.model.ExpenseRequest;
import com.ozandanis.expense.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExpenseRequestRepository {



    public Optional<ExpenseRequest> findById(int id) throws SQLException {
        String sql = "SELECT id, employee_id, category_id, amount, created_at, status, updated_at " +
                "FROM expense_request WHERE id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        }
    }

    public List<ExpenseRequest> findByStatus(String status) throws SQLException {
        List<ExpenseRequest> requests = new ArrayList<>();
        String sql = "SELECT er.*, e.name as employee_name, ec.name as category_name " +
                "FROM expense_request er " +
                "JOIN employee e ON er.employee_id = e.id " +
                "JOIN expense_category ec ON er.category_id = ec.id";

        if (status != null) {
            sql += " WHERE er.status = ?";
        }

        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (status != null) {
                ps.setString(1, status);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Employee employee = new Employee();
                employee.setId(rs.getInt("employee_id"));
                employee.setName(rs.getString("employee_name"));

                ExpenseCategory category = new ExpenseCategory();
                category.setId(rs.getInt("category_id"));
                category.setName(rs.getString("category_name"));

                ExpenseRequest request = new ExpenseRequest();
                request.setId(rs.getInt("id"));
                request.setEmployeeId(employee.getId());
                request.setCategoryId(category.getId());
                request.setAmount(rs.getBigDecimal("amount"));
                request.setStatus(rs.getString("status"));
                request.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime().toLocalDate());


                requests.add(request);
            }
        }
        return requests;
    }

    public List<ExpenseRequest> findByEmployeeId(int employeeId) throws SQLException {
        String sql = "SELECT id, employee_id, category_id, amount, created_at, status, updated_at " +
                "FROM expense_request WHERE employee_id = ?";
        List<ExpenseRequest> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<ExpenseRequest> findByEmployeeIdAndStatus(int employeeId, String status) throws SQLException {
        String sql = "SELECT id, employee_id, category_id, amount, created_at, status, updated_at " +
                "FROM expense_request WHERE employee_id = ? AND status = ?";
        List<ExpenseRequest> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setString(2, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }



    public float totalExpenses(int employee_id) throws SQLException {
        String sql = "SELECT SUM(amount) FROM expense_request WHERE employee_id = ?";

        try(Connection conn = JdbcUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employee_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getFloat(1);
            }

            return 0;
        }
    }

    public float totalPendingExpenses(int employee_id) throws SQLException {
        String sql = "SELECT SUM(amount) FROM expense_request WHERE employee_id = ? AND status = 'PENDING'";

        try(Connection conn = JdbcUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employee_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getFloat(1);
            }

            return 0;
        }
    }

    public float totalApprovedExpenses(int employee_id) throws SQLException {
        String sql = "SELECT SUM(amount) FROM expense_request WHERE employee_id = ? AND status = 'APPROVED'";

        try(Connection conn = JdbcUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employee_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getFloat(1);
            }

            return 0;
        }
    }

    public float totalRejectedExpenses(int employee_id) throws SQLException {
        String sql = "SELECT SUM(amount) FROM expense_request WHERE employee_id = ? AND status = 'REJECTED'";

        try(Connection conn = JdbcUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employee_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getFloat(1);
            }

            return 0;
        }
    }

    public void save(ExpenseRequest req) throws SQLException {
        String sql = "INSERT INTO expense_request(employee_id, category_id, amount, status) " +
                "VALUES(?, ?, ?, 'PENDING')";

        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, req.getEmployeeId());
            ps.setInt(2, req.getCategoryId());
            ps.setBigDecimal(3, req.getAmount());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating expense request failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    req.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating expense request failed, no ID obtained.");
                }
            }
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE expense_request SET status = ? WHERE id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public List<ExpenseRequest> getRecentExpenseRequests(int employeeId) throws SQLException {
        String sql = "SELECT er.id, er.employee_id, er.category_id, er.amount,  er.created_at, er.status, er.updated_at " +
                "FROM expense_request er " +
                "JOIN expense_category ec ON er.category_id = ec.id " +
                "WHERE er.employee_id = ? " +
                "ORDER BY er.created_at DESC " +
                "LIMIT 5";

        List<ExpenseRequest> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;

    }

    public BigDecimal sumApprovedByUnit(int unitId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(er.amount),0) AS total " +
                "FROM expense_request er " +
                "JOIN employee e ON er.employee_id = e.id " +
                "WHERE e.unit_id = ? AND er.status = 'APPROVED'";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, unitId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
            return BigDecimal.ZERO;
        }
    }

    private ExpenseRequest mapRow(ResultSet rs) throws SQLException {
        return new ExpenseRequest(
                rs.getInt("id"),
                rs.getInt("employee_id"),
                rs.getInt("category_id"),
                rs.getBigDecimal("amount"),
                rs.getDate("created_at").toLocalDate(),
                rs.getString("status"),
                rs.getDate("updated_at") != null ? rs.getDate("updated_at").toLocalDate() : null
        );
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM expense_request WHERE id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }



}
