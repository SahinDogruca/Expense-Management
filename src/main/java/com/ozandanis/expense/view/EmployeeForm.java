package com.ozandanis.expense.view;

import com.ozandanis.expense.model.Employee;
import com.ozandanis.expense.model.ExpenseCategory;
import com.ozandanis.expense.model.ExpenseRequest;
import com.ozandanis.expense.model.Reimbursement;
import com.ozandanis.expense.repository.EmployeeRepository;
import com.ozandanis.expense.repository.ExpenseCategoryRepository;
import com.ozandanis.expense.repository.ExpenseRequestRepository;
import com.ozandanis.expense.repository.ReimbursementRepository;
import com.ozandanis.expense.util.Helper;
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
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class EmployeeForm extends JFrame {
    private int employeeId;
    private String employeeName;
    private int unitId;

    // Components
    private JTabbedPane tabbedPane;
    private JTable expenseRequestsTable;
    private JTable reimbursementsTable;
    private JPanel profilePanel;
    private JPanel newExpensePanel;
    private JComboBox<CategoryItem> categoryComboBox;
    private JTextField amountField;
    private JLabel totalExpensesLabel;
    private JLabel pendingExpensesLabel;
    private JLabel approvedExpensesLabel;
    private JLabel rejectedExpensesLabel;
    private JLabel totalReimbursementsLabel;
    JTable recentTable;

    JPanel recentActivityPanel;

    private final ExpenseRequestRepository expenseRequestRepository = new ExpenseRequestRepository();
    private final ReimbursementRepository reimbursementRepository = new ReimbursementRepository();
    private final ExpenseCategoryRepository expenseCategoryRepository = new ExpenseCategoryRepository();
    private final EmployeeRepository employeeRepository = new EmployeeRepository();

    UserSession session = UserSession.getInstance();

    // Format for currency display
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

    public EmployeeForm() {
        this.employeeId = ((Employee) session.getCurrentUser()).getId();
        this.employeeName = ((Employee) session.getCurrentUser()).getName();
        this.unitId = ((Employee) session.getCurrentUser()).getUnitId();

        initializeUI();
        loadData();
    }

    private void initializeUI() {
        setTitle("Employee Dashboard - " + employeeName);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize tabbed pane
        tabbedPane = new JTabbedPane();

        // Create tabs
        createDashboardTab();
        createExpenseRequestsTab();
        createNewExpenseTab();
        createReimbursementsTab();
        createProfileTab();

        // Add tabbed pane to frame
        add(tabbedPane);
    }

    private void loadRecentExpenses() {
        DefaultTableModel model = (DefaultTableModel) recentTable.getModel();
        model.setRowCount(0); // Önce mevcut verileri temizle

        try {
            // ExpenseRepository üzerinden son 10 harcamayı al
            ExpenseRequestRepository repo = new ExpenseRequestRepository();
            List<ExpenseRequest> recentExpenses = repo.getRecentExpenseRequests(employeeId);



            for (ExpenseRequest expense : recentExpenses) {
                model.addRow(new Object[]{
                        expense.getCreated_at(), // LocalDate'i formatla
                        expense.getCategoryName(),
                        String.format("%.2f", expense.getAmount()),
                        expense.getStatus()
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading recent expenses: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createDashboardTab() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());

        // Summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Expense Summary"));

        totalExpensesLabel = new JLabel("Total Expenses: Loading...");
        pendingExpensesLabel = new JLabel("Pending Expenses: Loading...");
        approvedExpensesLabel = new JLabel("Approved Expenses: Loading...");
        rejectedExpensesLabel = new JLabel("Rejected Expenses: Loading...");
        totalReimbursementsLabel = new JLabel("Total Reimbursements: Loading...");

        summaryPanel.add(totalExpensesLabel);
        summaryPanel.add(pendingExpensesLabel);
        summaryPanel.add(approvedExpensesLabel);
        summaryPanel.add(rejectedExpensesLabel);
        summaryPanel.add(totalReimbursementsLabel);

        // Recent activity panel
        recentActivityPanel = new JPanel(new BorderLayout());
        recentActivityPanel.setBorder(BorderFactory.createTitledBorder("Recent Activity"));

        // Table for recent expenses
        String[] recentColumns = {"Date", "Category", "Amount", "Status"};
        DefaultTableModel recentModel = new DefaultTableModel(recentColumns, 0);
        recentTable = new JTable(recentModel);
        recentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane recentScrollPane = new JScrollPane(recentTable);

        recentActivityPanel.add(recentScrollPane, BorderLayout.CENTER);

        loadRecentExpenses();

        // Add components to dashboard
        dashboardPanel.add(summaryPanel, BorderLayout.NORTH);
        dashboardPanel.add(recentActivityPanel, BorderLayout.CENTER);

        // Quick actions panel
        JPanel quickActionsPanel = new JPanel();
        quickActionsPanel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));

        JButton newExpenseButton = new JButton("New Expense Request");
        newExpenseButton.addActionListener(e -> tabbedPane.setSelectedIndex(2)); // Go to New Expense tab

        JButton viewAllButton = new JButton("View All Expenses");
        viewAllButton.addActionListener(e -> tabbedPane.setSelectedIndex(1)); // Go to Expense Requests tab

        quickActionsPanel.add(newExpenseButton);
        quickActionsPanel.add(viewAllButton);

        dashboardPanel.add(quickActionsPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Dashboard", dashboardPanel);
    }

    private void createExpenseRequestsTab() {
        JPanel expenseRequestPanel = new JPanel(new BorderLayout());

        // Filter panel
        JPanel filterPanel = new JPanel();
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "PENDING", "APPROVED", "REJECTED", "CANCELLED"});
        filterPanel.add(new JLabel("Filter by Status:"));
        filterPanel.add(statusFilter);

        JButton filterButton = new JButton("Apply Filter");
        filterButton.addActionListener(e -> {
            String selectedStatus = statusFilter.getSelectedItem().toString();
            loadExpenseRequests(selectedStatus.equals("All") ? null : selectedStatus);
        });
        filterPanel.add(filterButton);

        // Table for expense requests
        String[] columns = {"ID", "Category", "Amount", "Status", "Created Date", "Last Updated"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        expenseRequestsTable = new JTable(model);
        expenseRequestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(expenseRequestsTable);

        // Action panel
        JPanel actionPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadExpenseRequests(null));

        JButton cancelButton = new JButton("Cancel Selected Request");
        cancelButton.addActionListener(e -> cancelSelectedExpenseRequest());

        actionPanel.add(refreshButton);
        actionPanel.add(cancelButton);

        // Add components to panel
        expenseRequestPanel.add(filterPanel, BorderLayout.NORTH);
        expenseRequestPanel.add(scrollPane, BorderLayout.CENTER);
        expenseRequestPanel.add(actionPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("My Expense Requests", expenseRequestPanel);
    }

    private void createNewExpenseTab() {
        newExpensePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Category selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        newExpensePanel.add(new JLabel("Expense Category:"), gbc);

        categoryComboBox = new JComboBox<>();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        newExpensePanel.add(categoryComboBox, gbc);

        // Amount field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        newExpensePanel.add(new JLabel("Amount:"), gbc);

        amountField = new JTextField(10);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        newExpensePanel.add(amountField, gbc);

        // Submit button
        JButton submitButton = new JButton("Submit Expense Request");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        newExpensePanel.add(submitButton, gbc);

        // Add action listener for submit button
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitExpenseRequest();
            }
        });

        // Add some padding around the form
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.add(newExpensePanel);

        tabbedPane.addTab("New Expense Request", wrapperPanel);
    }

    private void createReimbursementsTab() {
        JPanel reimbursementsPanel = new JPanel(new BorderLayout());

        // Table for reimbursements
        String[] columns = {"Expense ID", "Expense Category", "Expense Amount", "Reimbursed Amount", "Reimbursement Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reimbursementsTable = new JTable(model);
        reimbursementsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(reimbursementsTable);

        // Add components to panel
        reimbursementsPanel.add(scrollPane, BorderLayout.CENTER);

        // Action panel
        JPanel actionPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadReimbursements());
        actionPanel.add(refreshButton);

        reimbursementsPanel.add(actionPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("My Reimbursements", reimbursementsPanel);
    }

    private void createProfileTab() {
        profilePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        profilePanel.add(new JLabel("Name:"), gbc);

        JTextField nameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        profilePanel.add(nameField, gbc);

        // Unit field (read-only)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        profilePanel.add(new JLabel("Unit:"), gbc);

        JTextField unitField = new JTextField(20);
        unitField.setEditable(false);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        profilePanel.add(unitField, gbc);

        // Password fields
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        profilePanel.add(new JLabel("Current Password:"), gbc);

        JPasswordField currentPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        profilePanel.add(currentPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        profilePanel.add(new JLabel("New Password:"), gbc);

        JPasswordField newPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        profilePanel.add(newPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        profilePanel.add(new JLabel("Confirm New Password:"), gbc);

        JPasswordField confirmPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        profilePanel.add(confirmPasswordField, gbc);

        // Save button
        JButton saveButton = new JButton("Save Changes");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        profilePanel.add(saveButton, gbc);

        // Add action listener for save button
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProfileChanges(nameField.getText(),
                        new String(currentPasswordField.getPassword()),
                        new String(newPasswordField.getPassword()),
                        new String(confirmPasswordField.getPassword()));
            }
        });

        // Load user data
        // This will be implemented in loadProfileData() method

        // Add some padding around the form
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.add(profilePanel);

        tabbedPane.addTab("My Profile", wrapperPanel);
    }

    private void loadData() {
        loadExpenseRequests(null);
        loadCategories();
        loadReimbursements();
        loadProfileData();
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Here you would query the database to get summary data for the dashboard
        // For example:

        // 1. Query total expenses
        // SQL: SELECT SUM(amount) FROM expense_request WHERE employee_id = ?

        // 2. Query pending expenses
        // SQL: SELECT SUM(amount) FROM expense_request WHERE employee_id = ? AND status = 'PENDING'

        // 3. Query approved expenses
        // SQL: SELECT SUM(amount) FROM expense_request WHERE employee_id = ? AND status = 'APPROVED'

        // 4. Query rejected expenses
        // SQL: SELECT SUM(amount) FROM expense_request WHERE employee_id = ? AND status = 'REJECTED'

        // 5. Query total reimbursements
        // SQL: SELECT SUM(r.reimbursed_amount) FROM reimbursement r
        //      JOIN expense_request e ON r.expense_id = e.id
        //      WHERE e.employee_id = ?

        // 6. Query recent activity
        // SQL: SELECT er.created_at, ec.name, er.amount, er.status
        //      FROM expense_request er
        //      JOIN expense_category ec ON er.category_id = ec.id
        //      WHERE er.employee_id = ?
        //      ORDER BY er.created_at DESC
        //      LIMIT 5

        // Update the UI components with the fetched data
        // This is just a placeholder - implement actual database queries

        // For demonstration purposes, let's set some example data

        try {
            totalExpensesLabel.setText("Total Expenses: " + currencyFormat.format(expenseRequestRepository.totalExpenses(employeeId)));
            pendingExpensesLabel.setText("Pending Expenses: " + currencyFormat.format(expenseRequestRepository.totalPendingExpenses(employeeId)));
            approvedExpensesLabel.setText("Approved Expenses: " + currencyFormat.format(expenseRequestRepository.totalApprovedExpenses(employeeId)));
            rejectedExpensesLabel.setText("Rejected Expenses: " + currencyFormat.format(expenseRequestRepository.totalRejectedExpenses(employeeId)));
            totalReimbursementsLabel.setText("Total Reimbursements: " + currencyFormat.format(reimbursementRepository.totalReimbursement(employeeId)));
        } catch (SQLException e) {
            Helper.showError(this, "There is an error!!!");
        }

    }

    private void loadExpenseRequests(String statusFilter) {
        DefaultTableModel model = (DefaultTableModel) expenseRequestsTable.getModel();
        model.setRowCount(0); // Clear the table

        try {
            List<ExpenseRequest> expenseList;

            if (statusFilter != null && !statusFilter.equals("All")) {
                expenseList = expenseRequestRepository.findByEmployeeIdAndStatus(employeeId, statusFilter);
            } else {
                expenseList = expenseRequestRepository.findByEmployeeId(employeeId);
            }

            for (ExpenseRequest expense : expenseList) {
                model.addRow(new Object[]{
                        expense.getId(),
                        expense.getCategoryName(),
                        currencyFormat.format(expense.getAmount()),
                        expense.getStatus(),
                        expense.getCreated_at(),
                        expense.getUpdated_at() != null ? expense.getUpdated_at() : "-"
                });
            }
        } catch (SQLException e) {
            Helper.showError(this, "Error loading expense requests: " + e.getMessage());

        }
    }

    private void loadCategories() {
        categoryComboBox.removeAllItems();

        try {
            List<ExpenseCategory> categories = expenseCategoryRepository.findAll();
            for (ExpenseCategory category : categories) {
                categoryComboBox.addItem(new CategoryItem(category.getId(), category.getName()));
            }
        } catch (SQLException e) {
            Helper.showError(this, "Error loading expense categories: " + e.getMessage());
        }
    }

    private void loadReimbursements() {
        DefaultTableModel model = (DefaultTableModel) reimbursementsTable.getModel();
        model.setRowCount(0); // Clear the table

        try {
            List<Reimbursement> reimbursements = reimbursementRepository.getReimbursementsByEmployeeId(employeeId);
            for (Reimbursement reimb : reimbursements) {
                model.addRow(new Object[]{
                        reimb.getId(),
                        reimb.getCategoryName(),
                        reimb.getExpenseAmount(),
                        reimb.getReimbursedAmount(),
                        reimb.getReimbursementDate()
                });
            }


        } catch (SQLException e) {
            Helper.showError(this, "Error loading reimbursements: " + e.getMessage());
        }

    }

    private void loadProfileData() {
        this.employeeId = ((Employee) session.getCurrentUser()).getId();
        this.employeeName = ((Employee) session.getCurrentUser()).getName();
        this.unitId = ((Employee) session.getCurrentUser()).getUnitId();


        // Update the UI components with the fetched data
        // For demonstration purposes, let's set some example data
        JTextField nameField = (JTextField) findComponentByName(profilePanel, "nameField");
        JTextField unitField = (JTextField) findComponentByName(profilePanel, "unitField");

        try {
            if (nameField != null) {
                nameField.setText(employeeName);

            }

            if (unitField != null) {
                unitField.setText( ((Employee) session.getCurrentUser()).getUnitName() );

            }
        } catch (SQLException e) {
            Helper.showError(this, "Error loading profile data: " + e.getMessage());
        }


    }

    private Component findComponentByName(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container) {
                Component found = findComponentByName((Container) component, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void submitExpenseRequest() {
        try {
            // Get the selected category
            CategoryItem selectedCategory = (CategoryItem) categoryComboBox.getSelectedItem();
            if (selectedCategory == null) {
                Helper.showError(this, "Please select a category.");
                return;
            }

            // Get the amount
            String amountStr = amountField.getText().trim();
            if (amountStr.isEmpty()) {
                Helper.showError(this, "Please enter an amount.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Helper.showError(this, "Amount must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                Helper.showError(this, "Please enter a valid amount.");
                return;
            }

            ExpenseRequest expenseRequest = new ExpenseRequest();
            expenseRequest.setCategoryId(selectedCategory.getId());
            expenseRequest.setEmployeeId(employeeId);
            expenseRequest.setAmount(BigDecimal.valueOf(amount));
            expenseRequestRepository.save(expenseRequest);

            // Show success message
            Helper.showSuccess(this, "Expense request submitted successfully.");

            // Clear the form
            categoryComboBox.setSelectedIndex(0);
            amountField.setText("");

            // Refresh the expense requests table
            loadExpenseRequests(null);
            loadDashboardData();

        } catch (Exception e) {
            Helper.showError(this, "Error submitting expense request: " + e.getMessage());
        }
    }

    private void cancelSelectedExpenseRequest() {
        int selectedRow = expenseRequestsTable.getSelectedRow();
        if (selectedRow == -1) {
            Helper.showError(this, "Please select an expense request to cancel.");
            return;
        }

        int expenseId = (int) expenseRequestsTable.getValueAt(selectedRow, 0);
        String status = (String) expenseRequestsTable.getValueAt(selectedRow, 3);

        if (!"PENDING".equals(status)) {
            Helper.showError(this, "Only pending requests can be cancelled.");
            return;
        }


        boolean confirm = Helper.showConfirm(this, "Are you sure you want to cancel this expense request?");
        if (!confirm) {
            return;
        }


        try {
            expenseRequestRepository.updateStatus(expenseId, "CANCELLED");

            Helper.showSuccess(this, "Expense request cancelled successfully.");

            // Refresh the expense requests table
            loadExpenseRequests(null);
            loadDashboardData();
        } catch (SQLException e) {
            Helper.showError(this, "Error cancelling expense request: " + e.getMessage());
        }


    }

    private void saveProfileChanges(String name, String currentPassword, String newPassword, String confirmPassword) {
        // Validate inputs
        if (name.trim().isEmpty()) {
            Helper.showError(this, "Name cannot be empty.");
            return;
        }

        // If the user is changing their password
        if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            if (currentPassword.isEmpty()) {
                Helper.showError(this, "Current password cannot be empty.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Helper.showError(this, "New passwords do not match.");
                return;
            }

            if (newPassword.length() < 6) {
                Helper.showError(this, "New passwords must br at least 6 characters long.");
                return;
            }

            // Verify current password
            // SQL: SELECT COUNT(*) FROM employee WHERE id = ? AND password = ?
            // Note: In a real application, passwords should be hashed
            try {
                if(employeeRepository.Authenticate(employeeName, currentPassword).isPresent()) {
                    Employee employee = ((Employee) session.getCurrentUser());
                    if(employee != null) {
                        employee.setPassword(newPassword);
                        employeeRepository.update(employee);
                        session.setCurrentUser(employee);
                        EmployeeForm.this.employeeId = employee.getId();
                        EmployeeForm.this.employeeName = employee.getName();
                        setTitle("Employee Dashboard - " + employeeName);
                    } else {
                        Helper.showError(this, "There is an error.");
                    }

                }
            } catch (SQLException e) {
                Helper.showError(this, "Error saving profile changes: " + e.getMessage());
            }


        } else {
            try {
                Employee employee = ((Employee) session.getCurrentUser());
                if(employee != null) {
                    employee.setName(name);
                    employeeRepository.update(employee);
                    session.setCurrentUser(employee);
                    EmployeeForm.this.employeeId = employee.getId();
                    EmployeeForm.this.employeeName = employee.getName();
                    setTitle("Employee Dashboard - " + employeeName);
                } else {
                    Helper.showError(this, "There is an error.");
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        // Show success message
        Helper.showSuccess(this, "Profile updated successfully.");

        // Refresh profile data
        loadProfileData();
    }

    // Helper class for storing category information
    private static class CategoryItem {
        private final int id;
        private final String name;

        public CategoryItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
