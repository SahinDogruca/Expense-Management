# Expense Management System

## Overview
The Expense Management System is a Java Swing application designed to help organizations manage employee expenses, reimbursements, and budget tracking. The system provides different interfaces for managers and employees, allowing for efficient workflow management of expense requests and approvals.

## Features

### Employee Features
- **Dashboard**: View expense summaries and recent activity
- **Expense Requests**: Submit new expense requests and monitor their status
- **Reimbursements**: Track received reimbursements
- **Profile Management**: Update personal information and change password

### Manager Features
- **Employee Management**: Add and view employees
- **Unit Management**: Create and manage organizational units
- **Expense Category Management**: Define expense categories
- **Expense Request Approval**: Review and approve/reject employee expense requests
- **Reimbursement Tracking**: Monitor all reimbursements
- **Statistical Reports**: View expense statistics by employee, unit, and category
- **Profile Management**: Update personal information

## Database Schema

The application uses PostgreSQL with the following tables:

1. **unit**: Organizational units with budget limits
2. **employee**: Employee information including unit assignment and manager relationship
3. **expense_category**: Categories for expense classification
4. **expense_request**: Employee expense requests with approval workflow
5. **reimbursement**: Records of reimbursed expenses

## Technical Details

### Technology Stack
- Java SE
- Swing GUI Framework
- PostgreSQL Database
- JDBC for database connectivity

### System Requirements
- Java JDK 8 or higher
- PostgreSQL 9.6 or higher
- Minimum 4GB RAM
- 100MB available disk space

## Installation

### Database Setup
1. Install PostgreSQL if not already installed
2. Just run the project and database will be created.

### Application Setup
1. Clone the repository
2. Configure database connection in `config.properties`
3. Run the application

## Usage

### Login
- Use your employee ID and password to login
- The system will automatically direct you to either the Manager or Employee interface based on your role

### Employee Workflow
1. Submit expense requests with appropriate category and amount
2. Monitor request status (Pending, Approved, Rejected, Cancelled)
3. View reimbursements for approved expenses

### Manager Workflow
1. Review pending expense requests
2. Approve or reject requests based on company policy and budget constraints
3. Manage organizational structure and expense categories
4. Monitor expense statistics and budget utilization

## Security Features
- Password protection for all accounts
- Role-based access control
- Input validation to prevent SQL injection

## Future Enhancements
- Email notifications for expense status changes
- Document attachment support for receipts
- Mobile application support
- Integration with accounting software
- Multi-currency support

## Troubleshooting

### Common Issues
- **Database Connection Errors**: Verify database credentials in config.properties
- **Login Failures**: Ensure correct employee ID and password
- **Display Issues**: Check Java version compatibility

