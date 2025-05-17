package com.ozandanis.expense.util;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtil {
    private static final DatabaseInitializer initializer = new DatabaseInitializer();


    public static Connection getConnection() {
        try {
            String dbHost = initializer.getDbHost();
            String dbPort = initializer.getDbPort();
            String dbName = initializer.getDbName();
            String dbUser = initializer.getDbUser();
            String dbPassword = initializer.getDbPassword();


            String url = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);

            return DriverManager.getConnection(url, dbUser, dbPassword);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
