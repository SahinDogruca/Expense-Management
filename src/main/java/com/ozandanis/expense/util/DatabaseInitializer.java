package com.ozandanis.expense.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseInitializer {
    private final String dbHost;
    private final String dbPort;
    private final String dbUser;
    private final String dbPassword;
    private final String dbName;

    public DatabaseInitializer() {
        // Load configuration from .env file or environment variables
        Properties envProps = loadEnvProperties();
        this.dbHost = envProps.getProperty("DB_HOST", "localhost");
        this.dbPort = envProps.getProperty("DB_PORT", "5432");
        this.dbUser = envProps.getProperty("DB_USER", "postgres");
        this.dbPassword = envProps.getProperty("DB_PASSWORD", "sahker123");
        this.dbName = envProps.getProperty("DB_NAME");

        if (this.dbName == null || this.dbName.isEmpty()) {
            throw new RuntimeException("Database name (DB_NAME) must be specified in .env file");
        }
    }

    public String getDbHost() {
        return dbHost;
    }

    public String getDbPort() {
        return dbPort;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbName() {
        return dbName;
    }

    public void initializeDatabase() {
        try {
            // Step 1: Connect to PostgreSQL and create the database if it doesn't exist
            createDatabaseIfNotExists();



            System.out.println("Database initialization completed successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void createDatabaseIfNotExists() throws SQLException {
        // Connect to the default 'postgres' database to check/create our database
        String url = String.format("jdbc:postgresql://%s:%s/postgres", dbHost, dbPort);

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
             Statement stmt = conn.createStatement()) {

            // Check if database exists
            boolean dbExists = stmt.executeQuery(
                    String.format("SELECT 1 FROM pg_database WHERE datname = '%s'", dbName)
            ).next();

            if (!dbExists) {
                stmt.executeUpdate(String.format("CREATE DATABASE %s", dbName));
                System.out.println("Database '" + dbName + "' created successfully.");

                // Step 2: Connect to the specific database and run createDatabase.sql
                runSqlScript("src/main/resources/CreateDatabase.sql");

                // Step 3: Connect to the specific database and run initializeDatabase.sql
                runSqlScript("src/main/resources/InitializeDatabase.sql");

            } else {
                System.out.println("Database '" + dbName + "' already exists.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runSqlScript(String scriptPath) throws SQLException, IOException {
        String url = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
             Statement stmt = conn.createStatement()) {

            // Read the SQL script file
            String sqlScript = readFile(scriptPath);

            // Split the script into individual statements
            String[] sqlStatements = sqlScript.split(";\\s*\n");

            // Execute each statement
            for (String sql : sqlStatements) {
                if (!sql.trim().isEmpty()) {
                    stmt.executeUpdate(sql);
                }
            }

            System.out.println("Executed SQL script: " + scriptPath);
        }
    }

    private String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }

    private Properties loadEnvProperties() {
        Properties props = new Properties();

        try {
            // Try to load from .env file
            props.load(new FileReader(".env"));
        } catch (IOException e) {
            // If .env file doesn't exist, try to get from system environment
            System.out.println(".env file not found, using system environment variables");

            // Fall back to system environment variables
            props.setProperty("DB_HOST", System.getenv("DB_HOST"));
            props.setProperty("DB_PORT", System.getenv("DB_PORT"));
            props.setProperty("DB_USER", System.getenv("DB_USER"));
            props.setProperty("DB_PASSWORD", System.getenv("DB_PASSWORD"));
            props.setProperty("DB_NAME", System.getenv("DB_NAME"));
        }

        return props;
    }
}