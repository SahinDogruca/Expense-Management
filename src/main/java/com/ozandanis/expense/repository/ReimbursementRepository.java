package com.ozandanis.expense.repository;

import com.ozandanis.expense.model.Reimbursement;
import com.ozandanis.expense.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReimbursementRepository {

    public Optional<Reimbursement> findById(int id) throws SQLException {
        String sql = "SELECT id, expense_id, reimbursed_amount, reimbursement_date FROM reimbursement WHERE id = ?";
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



    public List<Reimbursement> getAll() throws SQLException {
        String sql = "SELECT * FROM reimbursement ORDER BY id";

        List<Reimbursement> reimbursements = new ArrayList<>();

        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reimbursements.add(mapRow(rs));
                }
            }
        }

        return reimbursements;
    }


    public List<Reimbursement> getReimbursementsByEmployeeId(int employeeId) throws SQLException {
        String sql = "SELECT r.expense_id, ec.name as category_name, er.amount, " +
                "r.reimbursed_amount, r.reimbursement_date " +
                "FROM reimbursement r " +
                "JOIN expense_request er ON r.expense_id = er.id " +
                "JOIN expense_category ec ON er.category_id = ec.id " +
                "WHERE er.employee_id = ? " +
                "ORDER BY r.reimbursement_date DESC";

        List<Reimbursement> reimbursements = new ArrayList<>();

        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mapRow(rs);
                }
            }
        }

        return reimbursements;
    }

    public List<Reimbursement> findByExpenseId(int expenseId) throws SQLException {
        String sql = "SELECT id, expense_id, reimbursed_amount, reimbursement_date FROM reimbursement WHERE expense_id = ?";
        List<Reimbursement> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public BigDecimal totalReimbursement(int employee_id) throws SQLException {
        String sql = "SELECT SUM(r.reimbursed_amount) FROM reimbursement r JOIN expense_request e ON r.expense_id = e.id WHERE e.employee_id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employee_id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                BigDecimal result = rs.getBigDecimal(1);
                // Handle NULL result from database
                return (result != null) ? result : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }



    public void save(Reimbursement r) throws SQLException {
        String sql = "INSERT INTO reimbursement(expense_id, reimbursed_amount, reimbursement_date) VALUES(?, ?, ?)";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getExpenseId());
            ps.setBigDecimal(2, r.getReimbursedAmount());
            ps.setDate(3, Date.valueOf(r.getReimbursementDate()));
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                r.setId(keys.getInt(1));
            }
        }
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM reimbursement WHERE id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Reimbursement mapRow(ResultSet rs) throws SQLException {
        return new Reimbursement(
                rs.getInt("id"),
                rs.getInt("expense_id"),
                rs.getBigDecimal("reimbursed_amount"),
                rs.getDate("reimbursement_date").toLocalDate()
        );
    }
}
