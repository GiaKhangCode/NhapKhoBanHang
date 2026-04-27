package com.thebangcoffee.dao;

import com.thebangcoffee.database.DatabaseConnection;
import com.thebangcoffee.model.CongThuc;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CongThucDAO {
    
    public List<CongThuc> getCongThucByBienThe(String maBienThe) {
        List<CongThuc> list = new ArrayList<>();
        String sql = "SELECT * FROM CONG_THUC WHERE MaBienThe = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBienThe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CongThuc ct = new CongThuc();
                    ct.setMaCongThuc(rs.getString("MaCongThuc"));
                    ct.setMaBienThe(rs.getString("MaBienThe"));
                    ct.setMaNL(rs.getString("MaNL"));
                    ct.setDinhLuong(rs.getDouble("DinhLuong"));
                    list.add(ct);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveCongThuc(String maBienThe, List<CongThuc> list) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Xóa công thức cũ
            String sqlDelete = "DELETE FROM CONG_THUC WHERE MaBienThe = ?";
            try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                psDel.setString(1, maBienThe);
                psDel.executeUpdate();
            }

            // 2. Thêm công thức mới
            String sqlInsert = "INSERT INTO CONG_THUC (MaCongThuc, MaBienThe, MaNL, DinhLuong) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                for (CongThuc ct : list) {
                    psIns.setString(1, "CT" + System.nanoTime()); // Tạo ID duy nhất
                    psIns.setString(2, maBienThe);
                    psIns.setString(3, ct.getMaNL());
                    psIns.setDouble(4, ct.getDinhLuong());
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}
