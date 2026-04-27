package com.thebangcoffee.dao;

import com.thebangcoffee.database.DatabaseConnection;
import java.sql.*;

public class ShiftDAO {

    public String getCurrentShift() {
        String sql = "SELECT MaCa FROM CA_LAM_VIEC WHERE TrangThai = 'Đang mở'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("MaCa");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean openShift(String nhanVien) {
        String sql = "INSERT INTO CA_LAM_VIEC (MaCa, NhanVienTruc, TrangThai) VALUES (?, ?, 'Đang mở')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "CA" + System.currentTimeMillis() / 1000);
            ps.setString(2, nhanVien);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double calculateShiftRevenue(String maCa) {
        String sql = "SELECT SUM(TongTien) AS Total FROM HOA_DON WHERE NgayTao >= (SELECT ThoiGianBatDau FROM CA_LAM_VIEC WHERE MaCa = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("Total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean closeShift(String maCa, double tongDoanhThu) {
        String sql = "UPDATE CA_LAM_VIEC SET TrangThai = 'Đã chốt', ThoiGianKetThuc = SYSTIMESTAMP, TongDoanhThu = ? WHERE MaCa = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, tongDoanhThu);
            ps.setString(2, maCa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
