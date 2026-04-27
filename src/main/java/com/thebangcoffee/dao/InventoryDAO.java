package com.thebangcoffee.dao;

import com.thebangcoffee.database.DatabaseConnection;
import com.thebangcoffee.model.NguyenLieu;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.thebangcoffee.model.PhieuNhapKho;
import com.thebangcoffee.model.ChiTietPhieuNhapKho;
import com.thebangcoffee.model.LoNguyenLieu;

public class InventoryDAO {
    
    public List<NguyenLieu> getAllNguyenLieu() {
        List<NguyenLieu> list = new ArrayList<>();
        String sql = "SELECT * FROM NGUYEN_LIEU ORDER BY MaNL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new NguyenLieu(
                    rs.getString("MaNL"),
                    rs.getString("TenNL"),
                    rs.getString("DVT_CoBan"),
                    rs.getDouble("SoLuongTon")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addNguyenLieu(NguyenLieu nl) {
        String sql = "INSERT INTO NGUYEN_LIEU (MaNL, TenNL, DVT_CoBan, SoLuongTon) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nl.getMaNL());
            ps.setString(2, nl.getTenNL());
            ps.setString(3, nl.getDvtCoBan());
            ps.setDouble(4, nl.getSoLuongTon());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStock(String maNL, double amountToAdd) {
        String sql = "UPDATE NGUYEN_LIEU SET SoLuongTon = SoLuongTon + ? WHERE MaNL = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amountToAdd);
            ps.setString(2, maNL);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteNguyenLieu(String maNL) {
        String sql = "DELETE FROM NGUYEN_LIEU WHERE MaNL = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNL);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean luuPhieuNhap(PhieuNhapKho pn, List<ChiTietPhieuNhapKho> ctList) {
        String insertPN = "INSERT INTO PHIEU_NHAP_KHO (MaPN, TongTien, GhiChu) VALUES (?, ?, ?)";
        String insertCT = "INSERT INTO CT_PHIEU_NHAP_KHO (MaPN, MaNL, SoLuong, DonGia, ThanhTien, TyLeQuyDoi) VALUES (?, ?, ?, ?, ?, ?)";
        String insertLo = "INSERT INTO LO_NGUYEN_LIEU (MaLo, MaNL, MaPN, SoLuongBanDau, SoLuongConLai, DonGiaQuyDoi, HanSuDung) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String updateStock = "UPDATE NGUYEN_LIEU SET SoLuongTon = SoLuongTon + ? WHERE MaNL = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction
            
            // 1. Thêm Phiếu Nhập
            try (PreparedStatement psPN = conn.prepareStatement(insertPN)) {
                psPN.setString(1, pn.getMaPN());
                psPN.setDouble(2, pn.getTongTien());
                psPN.setString(3, pn.getGhiChu());
                psPN.executeUpdate();
            }
            
            // 2. Thêm Chi Tiết, tạo Lô và Cập nhật Tồn Kho
            try (PreparedStatement psCT = conn.prepareStatement(insertCT);
                 PreparedStatement psLo = conn.prepareStatement(insertLo);
                 PreparedStatement psStock = conn.prepareStatement(updateStock)) {
                
                for (ChiTietPhieuNhapKho ct : ctList) {
                    psCT.setString(1, pn.getMaPN());
                    psCT.setString(2, ct.getMaNL());
                    psCT.setDouble(3, ct.getSoLuong());
                    psCT.setDouble(4, ct.getDonGia());
                    psCT.setDouble(5, ct.getThanhTien());
                    psCT.setDouble(6, ct.getTyLeQuyDoi());
                    psCT.addBatch();
                    
                    double thucNhap = ct.getSoLuong() * ct.getTyLeQuyDoi();
                    
                    // Tạo Lô hàng
                    String maLo = "LO" + System.currentTimeMillis() + ct.getMaNL();
                    psLo.setString(1, maLo);
                    psLo.setString(2, ct.getMaNL());
                    psLo.setString(3, pn.getMaPN());
                    psLo.setDouble(4, thucNhap);
                    psLo.setDouble(5, thucNhap);
                    double donGiaQuyDoi = (ct.getTyLeQuyDoi() > 0) ? (ct.getDonGia() / ct.getTyLeQuyDoi()) : 0;
                    psLo.setDouble(6, donGiaQuyDoi);
                    
                    if (ct.getHanSuDung() != null) {
                        psLo.setDate(7, new java.sql.Date(ct.getHanSuDung().getTime()));
                    } else {
                        psLo.setNull(7, Types.DATE);
                    }
                    psLo.addBatch();
                    
                    // Cập nhật Tồn Kho
                    psStock.setDouble(1, thucNhap);
                    psStock.setString(2, ct.getMaNL());
                    psStock.addBatch();
                }
                
                psCT.executeBatch();
                psLo.executeBatch();
                psStock.executeBatch();
            }
            
            conn.commit(); // Hoàn tất Transaction
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public List<LoNguyenLieu> getDanhSachLoByNL(String maNL) {
        List<LoNguyenLieu> list = new ArrayList<>();
        String sql = "SELECT * FROM LO_NGUYEN_LIEU WHERE MaNL = ? AND SoLuongConLai > 0 ORDER BY HanSuDung ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNL);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new LoNguyenLieu(
                        rs.getString("MaLo"),
                        rs.getString("MaNL"),
                        rs.getString("MaPN"),
                        rs.getDouble("SoLuongBanDau"),
                        rs.getDouble("SoLuongConLai"),
                        rs.getDouble("DonGiaQuyDoi"),
                        rs.getDate("HanSuDung")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean huyLoHang(String maLo, double soLuongHuy, String lyDo) {
        String insertPH = "INSERT INTO PHIEU_HUY_HANG (MaPhieuHuy, NgayHuy, GhiChu) VALUES (?, SYSDATE, ?)";
        String insertCT = "INSERT INTO CT_PHIEU_HUY_HANG (MaPhieuHuy, MaLo, SoLuongHuy, LyDo) VALUES (?, ?, ?, ?)";
        String updateLo = "UPDATE LO_NGUYEN_LIEU SET SoLuongConLai = SoLuongConLai - ? WHERE MaLo = ?";
        String updateTon = "UPDATE NGUYEN_LIEU SET SoLuongTon = SoLuongTon - ? WHERE MaNL = (SELECT MaNL FROM LO_NGUYEN_LIEU WHERE MaLo = ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            String maPH = "PH" + System.currentTimeMillis() / 1000;
            try (PreparedStatement ps = conn.prepareStatement(insertPH)) {
                ps.setString(1, maPH);
                ps.setString(2, "Hủy lô: " + maLo);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(insertCT)) {
                ps.setString(1, maPH);
                ps.setString(2, maLo);
                ps.setDouble(3, soLuongHuy);
                ps.setString(4, lyDo);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(updateLo)) {
                ps.setDouble(1, soLuongHuy);
                ps.setString(2, maLo);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(updateTon)) {
                ps.setDouble(1, soLuongHuy);
                ps.setString(2, maLo);
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex){}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (Exception ex){}
        }
    }

    public String huyPhieuNhapKho(String maPN) {
        String checkSql = "SELECT COUNT(*) AS UsedLots FROM LO_NGUYEN_LIEU WHERE MaPN = ? AND SoLuongConLai < SoLuongBanDau";
        String getLotsSql = "SELECT MaLo, MaNL, SoLuongBanDau FROM LO_NGUYEN_LIEU WHERE MaPN = ?";
        String delLoSql = "DELETE FROM LO_NGUYEN_LIEU WHERE MaLo = ?";
        String revertStockSql = "UPDATE NGUYEN_LIEU SET SoLuongTon = SoLuongTon - ? WHERE MaNL = ?";
        String delCTSql = "DELETE FROM CT_PHIEU_NHAP_KHO WHERE MaPN = ?";
        String delPNSql = "DELETE FROM PHIEU_NHAP_KHO WHERE MaPN = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            try(PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, maPN);
                try(ResultSet rs = ps.executeQuery()) {
                    if(rs.next() && rs.getInt("UsedLots") > 0) {
                        return "Không thể hủy! Lô hàng của phiếu này đã xuất bán một phần.";
                    }
                }
            }
            
            try(PreparedStatement psGet = conn.prepareStatement(getLotsSql);
                PreparedStatement psDelLo = conn.prepareStatement(delLoSql);
                PreparedStatement psRevStock = conn.prepareStatement(revertStockSql)) {
                psGet.setString(1, maPN);
                try(ResultSet rs = psGet.executeQuery()) {
                    while(rs.next()) {
                        String maLo = rs.getString("MaLo");
                        String maNL = rs.getString("MaNL");
                        double sl = rs.getDouble("SoLuongBanDau");
                        
                        psDelLo.setString(1, maLo);
                        psDelLo.executeUpdate();
                        
                        psRevStock.setDouble(1, sl);
                        psRevStock.setString(2, maNL);
                        psRevStock.executeUpdate();
                    }
                }
            }
            
            try(PreparedStatement psDelCT = conn.prepareStatement(delCTSql)) {
                psDelCT.setString(1, maPN);
                psDelCT.executeUpdate();
            }
            
            try(PreparedStatement psDelPN = conn.prepareStatement(delPNSql)) {
                psDelPN.setString(1, maPN);
                psDelPN.executeUpdate();
            }
            
            conn.commit();
            return null;
        } catch (SQLException e) {
            if(conn != null) try{conn.rollback();}catch(Exception ex){}
            e.printStackTrace();
            return "Lỗi hệ thống khi hủy phiếu!";
        } finally {
            if(conn != null) try{conn.setAutoCommit(true);}catch(Exception ex){}
        }
    }
}
