package com.ozandanis.expense.view;

import com.ozandanis.expense.model.*;
import com.ozandanis.expense.repository.*;
import com.ozandanis.expense.service.ReportingService;
import com.ozandanis.expense.util.Helper;
import com.ozandanis.expense.util.JdbcUtil;
import com.ozandanis.expense.util.StatisticItem;
import com.ozandanis.expense.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;
import java.util.Optional;

public class ManagerForm extends JFrame {
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;

    // Dashboard components
    private JPanel dashboardPanel;
    private JLabel welcomeLabel;
    private JPanel statsPanel;

    // Employee management components
    private JPanel employeePanel;
    private JTable employeeTable;
    private JButton addEmployeeButton;
    private JButton editEmployeeButton;
    private JScrollPane employeeScrollPane;

    // Unit management components
    private JPanel unitPanel;
    private JTable unitTable;
    private JButton addUnitButton;
    private JScrollPane unitScrollPane;

    // Expense category components
    private JPanel categoryPanel;
    private JTable categoryTable;
    private JButton addCategoryButton;
    private JScrollPane categoryScrollPane;

    // Expense request components
    private JPanel expenseRequestPanel;
    private JTable expenseRequestTable;
    private JButton approveButton;
    private JButton rejectButton;
    private JScrollPane expenseRequestScrollPane;
    private JComboBox<String> statusFilterComboBox;

    // Reimbursement components
    private JPanel reimbursementPanel;
    private JTable reimbursementTable;
    private JScrollPane reimbursementScrollPane;

    // Statistics components
    private JPanel statisticsPanel;
    private JComboBox<String> statsTypeComboBox;
    private JTable statsTable;
    private JScrollPane statsScrollPane;

    // Profile components
    private JPanel profilePanel;
    private JTextField nameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton updateProfileButton;

    private Connection connection;
    private int managerId;
    private String managerName;

    private final EmployeeRepository employeeRepository = new EmployeeRepository();
    private final ExpenseRequestRepository expenseRequestRepository = new ExpenseRequestRepository();
    private final ExpenseCategoryRepository expenseCategoryRepository = new ExpenseCategoryRepository();
    private final ReimbursementRepository reimbursementRepository = new ReimbursementRepository();
    private final UnitRepository unitRepository = new UnitRepository();

    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));
    private JTextField addCategoryText;


    UserSession session = UserSession.getInstance();


    public ManagerForm() {

        this.managerId = ((Employee) session.getCurrentUser()).getId();
        this.managerName = ((Employee) session.getCurrentUser()).getName();

        initComponents();
        loadData();

        setTitle("Expense Management System - Manager");
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout());

        // Initialize tabbed pane
        tabbedPane = new JTabbedPane();

        // Initialize all panels
        initDashboardPanel();
        initEmployeePanel();
        initUnitPanel();
        initCategoryPanel();
        initExpenseRequestPanel();
        initReimbursementPanel();
        initStatisticsPanel();
        initProfilePanel();

        // Add panels to tabbed pane
        tabbedPane.addTab("Dashboard", dashboardPanel);
        tabbedPane.addTab("Employees", employeePanel);
        tabbedPane.addTab("Units", unitPanel);
        tabbedPane.addTab("Expense Categories", categoryPanel);
        tabbedPane.addTab("Expense Requests", expenseRequestPanel);
        tabbedPane.addTab("Reimbursements", reimbursementPanel);
        tabbedPane.addTab("Statistics", statisticsPanel);
        tabbedPane.addTab("Profile", profilePanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void initDashboardPanel() {


        dashboardPanel = new JPanel(new BorderLayout(10, 10));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        welcomeLabel = new JLabel("Welcome, " + managerName + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        dashboardPanel.add(welcomeLabel, BorderLayout.NORTH);

        statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));

        // Summary cards will be added here when loadData is called

        dashboardPanel.add(statsPanel, BorderLayout.CENTER);
    }


    private void refreshEmployeeTable() {
        DefaultTableModel model = (DefaultTableModel) employeeTable.getModel();
        model.setRowCount(0);


        List<Employee> employees = null;
        try {
            employees = employeeRepository.findAllEmployee();

            for (Employee emp : employees) {
                model.addRow(new Object[]{
                        emp.getId(),
                        emp.getName(),
                        emp.getUnitName(),
                        emp.getManagerName() != null ? emp.getManagerName() : "N/A"
                });
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    private void showAddEmployeeDialog() {
        // Create dialog components
        JDialog dialog = new JDialog(this, "Add New Employee", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 300);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        JTextField nameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<Unit> unitCombo = new JComboBox<>();
        JComboBox<Employee> managerCombo = new JComboBox<>();

        // Populate unit combo (assuming you have a method to get units)
        // SELECT id, name FROM unit ORDER BY name
        List<Unit> units = null; // Example method
        try {
            units = unitRepository.getAllUnits();
            units.forEach(unitCombo::addItem);
        } catch (SQLException e) {
            Helper.showError(this, "There is an error!!!");
        }

        List<Employee> managers = null; // Example method
        try {
            managers = employeeRepository.getAllManagers();

            managerCombo.insertItemAt(null, 0);
            managers.forEach(managerCombo::addItem);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }



        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Unit:"));
        formPanel.add(unitCombo);
        formPanel.add(new JLabel("Manager (optional):"));
        formPanel.add(managerCombo);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String password = new String(passwordField.getPassword());
            Unit unit = (Unit) unitCombo.getSelectedItem();
            Employee manager = (Employee) managerCombo.getSelectedItem();

            if (name.isEmpty() || password.isEmpty() || unit == null) {
                Helper.showError(dialog, "Please fill all required fields!");
                return;
            }

            try {
                // INSERT INTO employee (name, password, unit_id, manager_id)
                // VALUES (?, ?, ?, ?)
                Employee employee = new Employee();
                employee.setName(name);
                employee.setPassword(password);
                employee.setUnitId(unit.getId());
                employee.setManagerId(manager != null ? manager.getId() : 0);
                employeeRepository.save(employee);

                refreshEmployeeTable();
                dialog.dispose();
            } catch (Exception ex) {
                Helper.showError(dialog, "Error saving employee: " + ex.getMessage());
                throw new RuntimeException(ex);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditEmployeeDialog(int employeeId) {
        // Get employee data
        // SELECT * FROM employee WHERE id = ?
        Optional<Employee> employeeOptional = null;
        Employee employee = null;
        try {
            employeeOptional = employeeRepository.findById(employeeId);
            if (employeeOptional.isPresent()) {
                employee = employeeOptional.get();



            } else {
                Helper.showError(this, "Employee not found!");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        // Create dialog components
        JDialog dialog = new JDialog(this, "Edit Employee", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 300);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        JTextField nameField = new JTextField(employee.getName());
        JPasswordField passwordField = new JPasswordField();
        JComboBox<Unit> unitCombo = new JComboBox<>();
        JComboBox<Employee> managerCombo = new JComboBox<>();

        // Populate unit combo
        // SELECT id, name FROM unit ORDER BY name
        List<Unit> units = null;
        try {
            units = unitRepository.getAllUnits();
            units.forEach(unitCombo::addItem);

            unitCombo.setSelectedItem(unitRepository.findById(employee.getUnitId()).get());

            List<Employee> managers = employeeRepository.getOtherManagers(employeeId);



            managerCombo.insertItemAt(null, 0);
            managers.forEach(managerCombo::addItem);


            managerCombo.setSelectedItem(employeeRepository.findById(employee.getManagerId()));


        } catch (SQLException e) {
            Helper.showError(this, "There is an error!!!");
            throw new RuntimeException(e);
        }



        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Password (leave blank to keep current):"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Unit:"));
        formPanel.add(unitCombo);
        formPanel.add(new JLabel("Manager:"));
        formPanel.add(managerCombo);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String password = new String(passwordField.getPassword());
            Unit unit = (Unit) unitCombo.getSelectedItem();
            Employee manager = (Employee) managerCombo.getSelectedItem();

            if (name.isEmpty() || unit == null) {
                Helper.showError(dialog, "Please fill all required fields!");
                return;
            }

            try {

                Employee dummyEmployee = employeeRepository.findById(employeeId).get();

                dummyEmployee.setName(name);

                dummyEmployee.setUnitId(unit.getId());
                dummyEmployee.setManagerId(manager != null ? manager.getId() : null);

                // If password is not empty, update it
                if (!password.isEmpty()) {

                    dummyEmployee.setPassword(password);
                    employeeRepository.update(dummyEmployee);
                } else {

                    employeeRepository.save(dummyEmployee);
                }

                refreshEmployeeTable();
                dialog.dispose();
            } catch (Exception ex) {
                Helper.showError(dialog, "Error updating employee: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private void initEmployeePanel() {
        employeePanel = new JPanel(new BorderLayout(10, 10));
        employeePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table for employees
        String[] employeeColumns = {"ID", "Name", "Unit", "Manager"};
        DefaultTableModel employeeModel = new DefaultTableModel(employeeColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        employeeTable = new JTable(employeeModel);
        employeeScrollPane = new JScrollPane(employeeTable);
        employeePanel.add(employeeScrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel employeeButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addEmployeeButton = new JButton("Add Employee");
        editEmployeeButton = new JButton("Edit Employee");

        employeeButtonPanel.add(addEmployeeButton);
        employeeButtonPanel.add(editEmployeeButton);

        employeePanel.add(employeeButtonPanel, BorderLayout.SOUTH);

        // Add action listeners
        addEmployeeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddEmployeeDialog();
            }
        });

        editEmployeeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = employeeTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int employeeId = (int) employeeTable.getValueAt(selectedRow, 0);
                    showEditEmployeeDialog(employeeId);
                } else {
                    Helper.showInfo(ManagerForm.this, "Please select an employee to edit.");
                }
            }
        });
    }


    private void showAddUnitDialog() {
        // Create dialog components
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Unit", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form fields
        JLabel nameLabel = new JLabel("Unit Name:");
        JTextField nameField = new JTextField();

        JLabel budgetLabel = new JLabel("Budget Limit:");
        JTextField budgetField = new JTextField();

        JLabel thresholdLabel = new JLabel("Threshold Limit:");
        JTextField thresholdField = new JTextField();
        thresholdField.setText("0"); // Default value

        // Add components to form
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(budgetLabel);
        formPanel.add(budgetField);
        formPanel.add(thresholdLabel);
        formPanel.add(thresholdField);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add panels to dialog
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Action listeners
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    Helper.showError(dialog, "Unit name cannot be empty");
                    return;
                }

                BigDecimal budgetLimit;
                BigDecimal thresholdLimit;

                try {
                    budgetLimit = new BigDecimal(budgetField.getText().trim());
                    thresholdLimit = new BigDecimal(thresholdField.getText().trim());
                } catch (NumberFormatException ex) {
                    Helper.showError(dialog, "Please enter valid numbers for limits");
                    return;
                }

                // Database operation to save unit
                // SQL: INSERT INTO unit(name, budget_limit, threshold_limit) VALUES (?, ?, ?)
                Unit unit = new Unit();
                unit.setName(name);
                unit.setBudgetLimit(budgetLimit);
                unit.setThresholdLimit(thresholdLimit);
                unitRepository.save(unit);
                Helper.showSuccess(ManagerForm.this, "Unit saved");

                dialog.dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                Helper.showError(dialog, "An unexpected error occurred");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    // Helper method to refresh the unit table after adding a new unit
    private void refreshUnitTable() {
        DefaultTableModel model = (DefaultTableModel) unitTable.getModel();
        model.setRowCount(0);


        List<Unit> units = null;
        try {
            units = unitRepository.getAllUnits();

            for (Unit unit : units) {
                model.addRow(new Object[]{
                        unit.getId(),
                        unit.getName(),
                        unit.getBudgetLimit(),
                        unit.getThresholdLimit()
                });
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }


    private void initUnitPanel() {
        unitPanel = new JPanel(new BorderLayout(10, 10));
        unitPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table for units
        String[] unitColumns = {"ID", "Name", "Budget Limit", "Threshold Limit"};
        DefaultTableModel unitModel = new DefaultTableModel(unitColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        unitTable = new JTable(unitModel);
        unitScrollPane = new JScrollPane(unitTable);
        unitPanel.add(unitScrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel unitButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addUnitButton = new JButton("Add Unit");



        unitButtonPanel.add(addUnitButton);

        unitPanel.add(unitButtonPanel, BorderLayout.SOUTH);

        // Add action listeners
        addUnitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddUnitDialog();
            }
        });
    }

    private void initCategoryPanel() {
        categoryPanel = new JPanel(new BorderLayout(10, 10));
        categoryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table for categories
        String[] categoryColumns = {"ID", "Name"};
        DefaultTableModel categoryModel = new DefaultTableModel(categoryColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        categoryTable = new JTable(categoryModel);
        categoryScrollPane = new JScrollPane(categoryTable);
        categoryPanel.add(categoryScrollPane, BorderLayout.CENTER);

        // Button panel - now with proper component order and sizing
        JPanel categoryButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        addCategoryText = new JTextField(15); // Set preferred width
        addCategoryButton = new JButton("Add Category");

        // Add components in correct order
        categoryButtonPanel.add(addCategoryText);
        categoryButtonPanel.add(addCategoryButton);

        categoryPanel.add(categoryButtonPanel, BorderLayout.SOUTH);

        // Add action listeners
        addCategoryButton.addActionListener(e -> {
            if(addCategoryText.getText().isEmpty()) {
                Helper.showInfo(ManagerForm.this, "Category name cannot be empty");
            } else {
                String categoryName = addCategoryText.getText().trim();
                ExpenseCategoryRepository expenseCategoryRepository = new ExpenseCategoryRepository();
                try {
                    expenseCategoryRepository.save(new ExpenseCategory(categoryName));
                    addCategoryText.setText(""); // Clear field after adding
                    refreshCategoryTable(); // Refresh the table to show new category
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Helper.showError(ManagerForm.this, "An unexpected error occurred");
                }
            }
        });
    }

    // Add this helper method to refresh the category table
    private void refreshCategoryTable() {
        DefaultTableModel model = (DefaultTableModel) categoryTable.getModel();
        model.setRowCount(0); // Clear existing data

        try {
            ExpenseCategoryRepository repo = new ExpenseCategoryRepository();
            List<ExpenseCategory> categories = repo.findAll();
            for (ExpenseCategory category : categories) {
                model.addRow(new Object[]{category.getId(), category.getName()});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            Helper.showError(this, "Error loading categories");
        }
    }

    private void refreshExpenseRequestTable() {
        DefaultTableModel model = (DefaultTableModel) expenseRequestTable.getModel();
        model.setRowCount(0); // Clear existing data

        String selectedStatus = (String) statusFilterComboBox.getSelectedItem();
        String statusFilter = "All".equals(selectedStatus) ? null : selectedStatus;

        try {
            ExpenseRequestRepository repo = new ExpenseRequestRepository();
            List<ExpenseRequest> requests = repo.findByStatus(statusFilter);

            for (ExpenseRequest request : requests) {
                model.addRow(new Object[]{
                        request.getId(),
                        request.getEmployeeName(), // Assuming Employee has getName()
                        request.getCategoryName(), // Assuming Category has getName()
                        request.getAmount(),
                        request.getStatus(),
                        request.getCreated_at() // Assuming this returns Date or formatted String
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            Helper.showError(this, "Error loading expense requests");
        }
    }




    // Onay butonu için tam işlev
    private void approveSelectedRequest() {
        int selectedRow = expenseRequestTable.getSelectedRow();
        if (selectedRow < 0) {
            Helper.showInfo(this, "Please select an expense request to approve");
            return;
        }

        int requestId = (int) expenseRequestTable.getValueAt(selectedRow, 0);
        try {
            ExpenseRequestRepository repo = new ExpenseRequestRepository();
            repo.updateStatus(requestId, "APPROVED");

            Helper.showInfo(this, "Expense request approved successfully");
            refreshExpenseRequestTable();

        } catch (SQLException ex) {
            ex.printStackTrace();
            Helper.showError(this, "Database error while approving request");
        }
    }

    // Red butonu için tam işlev
    private void rejectSelectedRequest() {
        int selectedRow = expenseRequestTable.getSelectedRow();
        if (selectedRow < 0) {
            Helper.showInfo(this, "Please select an expense request to reject");
            return;
        }

        int requestId = (int) expenseRequestTable.getValueAt(selectedRow, 0);
        try {
            ExpenseRequestRepository repo = new ExpenseRequestRepository();
            repo.updateStatus(requestId, "REJECTED");
            refreshExpenseRequestTable();
            Helper.showInfo(this, "Expense request rejected successfully");

        } catch (SQLException ex) {
            ex.printStackTrace();
            Helper.showError(this, "Database error while rejecting request");
        }
    }


    private void initExpenseRequestPanel() {
        expenseRequestPanel = new JPanel(new BorderLayout(10, 10));
        expenseRequestPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel filterLabel = new JLabel("Filter by Status:");
        statusFilterComboBox = new JComboBox<>(new String[]{"All", "PENDING", "APPROVED", "REJECTED", "CANCELLED"});

        filterPanel.add(filterLabel);
        filterPanel.add(statusFilterComboBox);

        expenseRequestPanel.add(filterPanel, BorderLayout.NORTH);

        // Table for expense requests
        String[] expenseColumns = {"ID", "Employee", "Category", "Amount", "Status", "Created At"};
        DefaultTableModel expenseModel = new DefaultTableModel(expenseColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        expenseRequestTable = new JTable(expenseModel);
        expenseRequestScrollPane = new JScrollPane(expenseRequestTable);
        expenseRequestPanel.add(expenseRequestScrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel expenseButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        approveButton = new JButton("Approve");
        rejectButton = new JButton("Reject");

        expenseButtonPanel.add(approveButton);
        expenseButtonPanel.add(rejectButton);

        expenseRequestPanel.add(expenseButtonPanel, BorderLayout.SOUTH);

        // Add action listeners
        statusFilterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshExpenseRequestTable();
            }
        });

        approveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                approveSelectedRequest();
            }
        });

        rejectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rejectSelectedRequest();
            }
        });
    }

    private void refreshReimbursementTable() {
        DefaultTableModel model = (DefaultTableModel) reimbursementTable.getModel();
        model.setRowCount(0); // Clear existing data

        try {
            // Assuming you have a ReimbursementRepository class
            ReimbursementRepository repo = new ReimbursementRepository();
            List<Reimbursement> reimbursements = repo.getAll();

            for (Reimbursement reimbursement : reimbursements) {
                model.addRow(new Object[]{
                        reimbursement.getId(),
                        reimbursement.getExpenseId(),
                        reimbursement.getEmployeeName(),
                        reimbursement.getExpenseAmount(),
                        reimbursement.getReimbursedAmount(),
                        reimbursement.getReimbursementDate()
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            Helper.showError(this, "Error loading reimbursements");
        }
    }

    private void refreshStatistics() {
        DefaultTableModel model = (DefaultTableModel) statsTable.getModel();
        model.setRowCount(0); // Clear existing data

        String selectedType = (String) statsTypeComboBox.getSelectedItem();

        try {
            ReportingService reportingService = new ReportingService();
            List<StatisticItem> stats = null;
            switch (selectedType) {
                case "Expenses by Employee":
                    stats = reportingService.getExpensesByEmployee();

                    break;

                case "Expenses by Unit":

                    stats = reportingService.getExpensesByUnit();
                    break;

                case "Expenses by Category":
                    stats = reportingService.getExpensesByCategory();
                    break;

                case "Monthly Expenses":
                    stats = reportingService.getMonthlyExpenses();
                    break;
            }

            // Assuming all your functions return List<StatisticItem> with getName(), getTotalAmount(), getCount()
            for (StatisticItem item : stats) {
                 model.addRow(new Object[]{
                    item.getName(),
                    item.getTotalAmount(),
                     item.getCount()
                 });
             }

        } catch (SQLException ex) {
            ex.printStackTrace();
            Helper.showError(this, "Error loading statistics: " + ex.getMessage());
        }
    }

    private void initReimbursementPanel() {
        reimbursementPanel = new JPanel(new BorderLayout(10, 10));
        reimbursementPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table for reimbursements
        String[] reimbursementColumns = {"ID", "Expense ID", "Employee", "Amount", "Reimbursed Amount", "Date"};
        DefaultTableModel reimbursementModel = new DefaultTableModel(reimbursementColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reimbursementTable = new JTable(reimbursementModel);
        reimbursementScrollPane = new JScrollPane(reimbursementTable);
        reimbursementPanel.add(reimbursementScrollPane, BorderLayout.CENTER);

        refreshReimbursementTable();
    }

    private void initStatisticsPanel() {
        statisticsPanel = new JPanel(new BorderLayout(10, 10));
        statisticsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filter panel
        JPanel statsFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statsTypeLabel = new JLabel("Statistics Type:");
        statsTypeComboBox = new JComboBox<>(new String[]{
                "Expenses by Employee",
                "Expenses by Unit",
                "Expenses by Category",
                "Monthly Expenses"
        });

        statsFilterPanel.add(statsTypeLabel);
        statsFilterPanel.add(statsTypeComboBox);

        statisticsPanel.add(statsFilterPanel, BorderLayout.NORTH);

        // Table for statistics
        String[] statsColumns = {"Name", "Total Amount", "Count"};
        DefaultTableModel statsModel = new DefaultTableModel(statsColumns, 0);
        statsTable = new JTable(statsModel);
        statsScrollPane = new JScrollPane(statsTable);
        statisticsPanel.add(statsScrollPane, BorderLayout.CENTER);

        // Add action listeners
        statsTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshStatistics();
            }
        });
    }

    private void initProfilePanel() {
        profilePanel = new JPanel(new BorderLayout(10, 10));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField();

        JLabel passwordLabel = new JLabel("New Password:");
        passwordField = new JPasswordField();

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordField = new JPasswordField();

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(confirmPasswordLabel);
        formPanel.add(confirmPasswordField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        updateProfileButton = new JButton("Update Profile");
        buttonPanel.add(updateProfileButton);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add some padding
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(contentPanel, BorderLayout.NORTH);

        profilePanel.add(wrapperPanel, BorderLayout.CENTER);

        // Add action listeners
        updateProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Employee employee = null;
                try {
                    employee = ((Employee) session.getCurrentUser());


                    if(!nameField.getText().isEmpty()) {
                        employee.setName(nameField.getText());

                        if(!passwordField.getText().isEmpty()) {

                            if(passwordField.getText().equals(confirmPasswordField.getText())) {
                                employee.setPassword(passwordField.getText());

                                employeeRepository.update(employee);
                                Helper.showSuccess(ManagerForm.this, "Profile Updated");


                                session.setCurrentUser(employee);

                                System.out.println(employee.toString());

                                ManagerForm.this.managerId = ((Employee) session.getCurrentUser()).getId();
                                ManagerForm.this.managerName = ((Employee) session.getCurrentUser()).getName();

                                welcomeLabel.setText("Welcome, " + ManagerForm.this.managerName + "!");

                            } else {
                                Helper.showInfo(ManagerForm.this, "Passwords do not match");
                            }
                        } else {

                            employeeRepository.update(employee);
                            Helper.showSuccess(ManagerForm.this, "Profile Updated");


                            session.setCurrentUser(employee);

                            System.out.println(employee.toString());

                            ManagerForm.this.managerId = ((Employee) session.getCurrentUser()).getId();
                            ManagerForm.this.managerName = ((Employee) session.getCurrentUser()).getName();


                            System.out.println(managerId);
                            System.out.println(managerName);

                            welcomeLabel.setText("Welcome, " + ManagerForm.this.managerName + "!");
                        }
                    } else {
                        Helper.showInfo(ManagerForm.this, "Name field is empty");
                    }


                } catch (SQLException ex) {
                    Helper.showError(ManagerForm.this, "Error loading profile: " + ex.getMessage());
                    throw new RuntimeException(ex);

                }




            }
        });
    }

    private void loadData() {
        loadDashboardStats();
        refreshExpenseRequestTable();
        refreshCategoryTable();
        refreshEmployeeTable();
        refreshUnitTable();
        refreshReimbursementTable();
        refreshStatistics();
    }

    private void loadDashboardStats() {
        statsPanel.removeAll();

        try {
            Connection connection = JdbcUtil.getConnection();
            // Total pending requests
            PreparedStatement pendingStmt = connection.prepareStatement(
                    "SELECT COUNT(*) FROM expense_request WHERE status = 'PENDING'");
            ResultSet pendingRs = pendingStmt.executeQuery();
            int pendingCount = 0;
            if (pendingRs.next()) {
                pendingCount = pendingRs.getInt(1);
            }

            // Total approved requests
            PreparedStatement approvedStmt = connection.prepareStatement(
                    "SELECT COUNT(*) FROM expense_request WHERE status = 'APPROVED'");
            ResultSet approvedRs = approvedStmt.executeQuery();
            int approvedCount = 0;
            if (approvedRs.next()) {
                approvedCount = approvedRs.getInt(1);
            }

            // Total expense amount
            PreparedStatement totalExpenseStmt = connection.prepareStatement(
                    "SELECT SUM(amount) FROM expense_request WHERE status = 'APPROVED'");
            ResultSet totalExpenseRs = totalExpenseStmt.executeQuery();
            double totalExpense = 0;
            if (totalExpenseRs.next() && totalExpenseRs.getObject(1) != null) {
                totalExpense = totalExpenseRs.getDouble(1);
            }

            // Total reimbursement amount
            PreparedStatement totalReimbursementStmt = connection.prepareStatement(
                    "SELECT SUM(reimbursed_amount) FROM reimbursement");
            ResultSet totalReimbursementRs = totalReimbursementStmt.executeQuery();
            double totalReimbursement = 0;
            if (totalReimbursementRs.next() && totalReimbursementRs.getObject(1) != null) {
                totalReimbursement = totalReimbursementRs.getDouble(1);
            }

            // Add stats cards
            addStatCard("Pending Requests", String.valueOf(pendingCount));
            addStatCard("Approved Requests", String.valueOf(approvedCount));
            addStatCard("Total Expenses", currencyFormat.format(totalExpense));
            addStatCard("Total Reimbursements", currencyFormat.format(totalReimbursement));

        } catch (SQLException e) {

            Helper.showError(this, "Error loading dashboard statistics: " + e.getMessage());
        }

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private void addStatCard(String title, String value) {
        JPanel cardPanel = new JPanel(new BorderLayout(5, 5));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        cardPanel.setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));

        cardPanel.add(titleLabel, BorderLayout.NORTH);
        cardPanel.add(valueLabel, BorderLayout.CENTER);

        statsPanel.add(cardPanel);
    }


}