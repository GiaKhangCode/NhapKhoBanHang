package com.thebangcoffee.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class quản lý kết nối đến Oracle Database.
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:ORCL";
    private static final String USER = "NhapKhoBanHang"; // Thay đổi user của bạn
    private static final String PASSWORD = "Admin123"; // Thay đổi password của bạn
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy Driver Oracle: " + e.getMessage());
        }
    }
    
}
