package ru.linachan.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLDBManager {

    private Connection dbConnection;

    public MySQLDBManager(String dbDriver, String dbUrl, String dbUser, String dbPassword) throws ClassNotFoundException, SQLException {
        Class.forName(dbDriver);
        dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public void disconnect() throws SQLException {
        if (dbConnection != null) {
            dbConnection.close();
        }
    }

    public <T extends MySQLTable> MySQLQuery<T> query(Class<T> dbModel) {
        return new MySQLQuery<>(this);
    }

    public Statement getStatement() throws SQLException {
        return dbConnection.createStatement();
    }
}
