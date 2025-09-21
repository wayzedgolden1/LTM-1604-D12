package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=DictionaryDB;encrypt=false";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456789"; // Thay bằng mật khẩu sa của bạn

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
