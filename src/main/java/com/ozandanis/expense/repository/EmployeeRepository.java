package com.ozandanis.expense.repository;

import com.ozandanis.expense.model.Employee;
import com.ozandanis.expense.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeRepository {

    public Optional<Employee> findById(int id) throws SQLException {
        String sql = "SELECT id, name, password, unit_id, manager_id FROM employee WHERE id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getInt("unit_id"),
                        (Integer) rs.getObject("manager_id")
                ));
            }
            return Optional.empty();
        }
    }


    public List<Employee> findAllEmployee() throws SQLException {
        String sql = "SELECT id, name, password, unit_id, manager_id FROM employee";
        List<Employee> employees = null;

        try (Connection conn = JdbcUtil.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            employees = new ArrayList<>();

            while (rs.next()) {
                employees.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getInt("unit_id"),
                        (Integer) rs.getObject("manager_id")
                ));
            }
        }

        return employees;
    }



    public Optional<Employee> Authenticate(String name, String password) throws SQLException {
        String sql = "SELECT id, name, password, unit_id, manager_id FROM employee WHERE name = ? and password = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return  Optional.of(new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getInt("unit_id"),
                        (Integer) rs.getObject("manager_id")
                ));


            }
            return Optional.empty();
        }
    }

    public List<Employee> findByUnitId(int unitId) throws SQLException {
        String sql = "SELECT id, name, password, unit_id, manager_id FROM employee WHERE unit_id = ?";
        List<Employee> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, unitId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getInt("unit_id"),
                        (Integer) rs.getObject("manager_id")
                ));
            }
        }
        return list;
    }


    public List<Employee> getAllManagers() throws SQLException {
        String sql = "SELECT id, name, password, unit_id, manager_id FROM employee WHERE manager_id IS NULL";
        List<Employee> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getInt("unit_id"),
                        (Integer) rs.getObject("manager_id")
                ));
            }
        }
        return list;
    }

    public List<Employee> getOtherManagers(int employee_id) throws SQLException {
        String sql = "SELECT id, name, password, unit_id, manager_id FROM employee WHERE id != ? AND manager_id IS NULL";
        List<Employee> list = new ArrayList<>();
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employee_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getInt("unit_id"),
                        (Integer) rs.getObject("manager_id")
                ));
            }
        }
        return list;
    }

    public void save(Employee employee) throws SQLException {
        String sql = "INSERT INTO employee(name, password, unit_id, manager_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, employee.getName());
            ps.setString(2, employee.getPassword());
            ps.setInt(3, employee.getUnitId());
            if (employee.getManagerId() != 0) {
                ps.setInt(4, employee.getManagerId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                employee.setId(keys.getInt(1));
            }
        }
    }

    public void update(Employee employee) throws SQLException {
        String sql = "UPDATE employee SET name = ?, unit_id = ?, manager_id = ?, password = ? WHERE id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, employee.getName());
            ps.setInt(2, employee.getUnitId());

            // Handle manager_id properly
            if (employee.getManagerId() != null && employee.getManagerId() > 0) {
                // First verify the manager exists
                if (managerExists(conn, employee.getManagerId())) {
                    ps.setInt(3, employee.getManagerId());
                } else {
                    throw new SQLException("Manager with ID " + employee.getManagerId() + " does not exist");
                }
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setString(4, employee.getPassword());
            ps.setInt(5, employee.getId());

            ps.executeUpdate();
        }
    }

    // Helper method to check if manager exists
    private boolean managerExists(Connection conn, int managerId) throws SQLException {
        String sql = "SELECT 1 FROM employee WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Returns true if manager exists
            }
        }
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM employee WHERE id = ?";
        try (Connection conn = JdbcUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
