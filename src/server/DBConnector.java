package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    // Nếu muốn bảo mật, thay bằng đọc từ biến môi trường
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=DictionaryDB2;encrypt=false";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456789"; // thay bằng mật khẩu của bạn

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
