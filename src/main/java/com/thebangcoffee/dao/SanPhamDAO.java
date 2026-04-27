package com.thebangcoffee.dao;

import com.thebangcoffee.database.DatabaseConnection;
import com.thebangcoffee.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SanPhamDAO {
    
    public List<SanPham> getAllSanPham() {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SAN_PHAM";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SanPham sp = new SanPham(rs.getString("MaSP"), rs.getString("TenSP"));
                sp.setDanhSachBienThe(getBienTheBySP(sp.getMaSP()));
                sp.setAllowedToppings(getToppingsForSP(sp.getMaSP()));
                list.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BienTheSP> getBienTheBySP(String maSP) {
        List<BienTheSP> list = new ArrayList<>();
        String sql = "SELECT * FROM BIEN_THE_SP WHERE MaSP = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BienTheSP(
                        rs.getString("MaBienThe"),
                        rs.getString("MaSP"),
                        rs.getString("TenSize"),
                        rs.getLong("GiaBan")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Topping> getAllTopping() {
        List<Topping> list = new ArrayList<>();
        String sql = "SELECT * FROM TOPPING";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Topping(
                    rs.getString("MaTopping"),
                    rs.getString("TenTopping"),
                    rs.getLong("GiaBan"),
                    rs.getString("MaNL_LienKet"),
                    rs.getDouble("DinhLuongHaoHut")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public boolean insertSanPham(SanPham sp) {
        String sql = "INSERT INTO SAN_PHAM (MaSP, TenSP) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sp.getMaSP());
            ps.setString(2, sp.getTenSP());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSanPham(String maSP) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            conn.setAutoCommit(false);
            // Xóa topping liên kết trước
            try (PreparedStatement ps0 = conn.prepareStatement("DELETE FROM SP_TOPPING WHERE MaSP = ?")) {
                ps0.setString(1, maSP);
                ps0.executeUpdate();
            }
            // Xóa biến thể
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM BIEN_THE_SP WHERE MaSP = ?")) {
                ps1.setString(1, maSP);
                ps1.executeUpdate();
            }
            // Xóa sản phẩm
            try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM SAN_PHAM WHERE MaSP = ?")) {
                ps2.setString(1, maSP);
                ps2.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertBienThe(BienTheSP bt) {
        String sql = "INSERT INTO BIEN_THE_SP (MaBienThe, MaSP, TenSize, GiaBan) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bt.getMaBienThe());
            ps.setString(2, bt.getMaSP());
            ps.setString(3, bt.getTenSize());
            ps.setLong(4, bt.getGiaBan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBienThe(BienTheSP bt) {
        String sql = "UPDATE BIEN_THE_SP SET TenSize = ?, GiaBan = ? WHERE MaBienThe = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bt.getTenSize());
            ps.setLong(2, bt.getGiaBan());
            ps.setString(3, bt.getMaBienThe());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBienThe(String maBienThe) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM CONG_THUC WHERE MaBienThe = ?")) {
                ps.setString(1, maBienThe);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM BIEN_THE_SP WHERE MaBienThe = ?")) {
                ps.setString(1, maBienThe);
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveTopping(Topping tp) {
        String sql = "MERGE INTO TOPPING t USING DUAL ON (t.MaTopping = ?) " +
                     "WHEN MATCHED THEN UPDATE SET TenTopping = ?, GiaBan = ?, MaNL_LienKet = ?, DinhLuongHaoHut = ? " +
                     "WHEN NOT MATCHED THEN INSERT (MaTopping, TenTopping, GiaBan, MaNL_LienKet, DinhLuongHaoHut) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tp.getMaTopping());
            ps.setString(2, tp.getTenTopping());
            ps.setLong(3, tp.getGiaBan());
            ps.setString(4, tp.getMaNL_LienKet());
            ps.setDouble(5, tp.getDinhLuongHaoHut());
            ps.setString(6, tp.getMaTopping());
            ps.setString(7, tp.getTenTopping());
            ps.setLong(8, tp.getGiaBan());
            ps.setString(9, tp.getMaNL_LienKet());
            ps.setDouble(10, tp.getDinhLuongHaoHut());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTopping(String maTopping) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM SP_TOPPING WHERE MaTopping = ?")) {
                ps1.setString(1, maTopping);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM TOPPING WHERE MaTopping = ?")) {
                ps2.setString(1, maTopping);
                ps2.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getToppingsForSP(String maSP) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MaTopping FROM SP_TOPPING WHERE MaSP = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("MaTopping"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveSPToppings(String maSP, List<String> toppingIds) {
        String delSql = "DELETE FROM SP_TOPPING WHERE MaSP = ?";
        String insSql = "INSERT INTO SP_TOPPING (MaSP, MaTopping) VALUES (?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement psDel = conn.prepareStatement(delSql)) {
                psDel.setString(1, maSP);
                psDel.executeUpdate();
            }
            
            try (PreparedStatement psIns = conn.prepareStatement(insSql)) {
                for (String tId : toppingIds) {
                    psIns.setString(1, maSP);
                    psIns.setString(2, tId);
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
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
}
