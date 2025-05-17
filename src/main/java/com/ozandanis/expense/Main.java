package com.ozandanis.expense;

import com.ozandanis.expense.util.DatabaseInitializer;
import com.ozandanis.expense.view.Login;

import javax.swing.*;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {

        DatabaseInitializer dbInitializer = new DatabaseInitializer();
        dbInitializer.initializeDatabase();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Login().setVisible(true);
            }
        });


    }
}