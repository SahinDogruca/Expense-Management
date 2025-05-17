package com.ozandanis.expense.model;

import com.ozandanis.expense.repository.EmployeeRepository;
import com.ozandanis.expense.repository.UnitRepository;

import java.sql.SQLException;

public class Employee {
    private int id;
    private String name;
    private String password;
    private int unitId;
    private Integer managerId;

    public Employee() {}

    public Employee(int id, String name, String password, int unitId ,Integer managerId) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.unitId = unitId;
        this.managerId = managerId;
    }

    public Employee(int id, String name, int unitId, Integer managerId) {
        this.id = id;
        this.name = name;
        this.unitId = unitId;
        this.managerId = managerId;
    }

    public Employee(String name, int unitId, Integer managerId) {
        this.name = name;
        this.unitId = unitId;
        this.managerId = managerId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getUnitId() { return unitId; }
    public void setUnitId(int unitId) { this.unitId = unitId; }
    public Integer getManagerId() { return managerId != null ? managerId : 0; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }
    public int isAdmin() {
        if(managerId == null) {
            return 1;
        } else {
            return 0;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUnitName() throws SQLException {
        UnitRepository unitRepository = new UnitRepository();
        return unitRepository.findById(this.getUnitId()).isPresent() ? unitRepository.findById(this.getUnitId()).get().getName() : "";
    }

    public String getManagerName() throws SQLException {
        EmployeeRepository employeeRepository = new EmployeeRepository();
        return employeeRepository.findById(this.getManagerId()).isPresent() ? employeeRepository.findById(this.getManagerId()).get().getName() : null;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", unitId=" + unitId +
                ", managerId=" + managerId +
                '}';
    }
}
