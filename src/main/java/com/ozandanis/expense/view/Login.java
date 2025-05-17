package com.ozandanis.expense.view;

import com.ozandanis.expense.model.Employee;
import com.ozandanis.expense.repository.EmployeeRepository;
import com.ozandanis.expense.util.Helper;
import com.ozandanis.expense.util.JdbcUtil;
import com.ozandanis.expense.util.UserSession;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

public class Login extends JFrame{
    private JPanel pnlLogin;
    private JTextField txtName;
    private JPasswordField txtPass;
    private JButton btnLogin;
    private JLabel lblName;
    private JLabel lblPassword;


    public Login() {
        add(pnlLogin);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Login");



        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                char[] passwordChars = txtPass.getPassword();
                try {
                    String pass = new String(passwordChars);
                    String name = txtName.getText();

                    if(pass.isEmpty() || name.isEmpty()) {
                        Helper.showError(pnlLogin, "Alanları boş bırakmayınız!");
                    } else {
                        EmployeeRepository employeeRepository = new EmployeeRepository();
                        Optional<Employee> optionalEmployee = employeeRepository.Authenticate(name, pass);

                        if(optionalEmployee.isPresent()) {
                            Employee employee = optionalEmployee.get();

                            UserSession.getInstance().setCurrentUser(employee);
                            UserSession.getInstance().setAttribute("isAdmin", employee.isAdmin());

                            if(employee.isAdmin() > 0) {
                                new ManagerForm().setVisible(true);

                            } else {


                                new EmployeeForm().setVisible(true);
                            }
                            dispose();
                        } else {
                            Helper.showError(pnlLogin, "Bir hata oluştu. Bilgileri kontrol ediniz!!!");
                        }
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    Arrays.fill(passwordChars, '\0');
                }
            }
        });
    }
}
