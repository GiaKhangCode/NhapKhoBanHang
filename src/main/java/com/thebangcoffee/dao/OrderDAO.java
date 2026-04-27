package com.thebangcoffee.dao;

import com.thebangcoffee.database.DatabaseConnection;
import com.thebangcoffee.model.*;
import java.sql.*;
import java.util.UUID;

public class OrderDAO {

    /**
     * Thực hiện thanh toán hóa đơn và trừ kho tự động trong một Transaction.
     */
    public boolean processCheckout(HoaDon hoaDon) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Lưu Hóa Đơn
            String sqlHD = "INSERT INTO HOA_DON (MaHD, TongTien, TrangThai, NgayTao) VALUES (?, ?, ?, SYSDATE)";
            try (PreparedStatement psHD = conn.prepareStatement(sqlHD)) {
                psHD.setString(1, hoaDon.getMaHD());
                psHD.setLong(2, hoaDon.getTongTien());
                psHD.setString(3, "Hoàn tất");
                psHD.executeUpdate();
            }

            // 2. Lưu Chi tiết hóa đơn và xử lý trừ kho
            String sqlCTHD = "INSERT INTO CT_HOA_DON (MaCTHD, MaHD, MaBienThe, SoLuong, DonGia) VALUES (?, ?, ?, ?, ?)";
            String sqlCTTopping = "INSERT INTO CT_TOPPING (MaCTHD, MaTopping, DonGiaTopping) VALUES (?, ?, ?)";
            
            for (CTHoaDon ct : hoaDon.getChiTietHoaDon()) {
                String maCTHD = UUID.randomUUID().toString().substring(0, 20); // Tạo ID tạm
                ct.setMaCTHD(maCTHD);

                try (PreparedStatement psCT = conn.prepareStatement(sqlCTHD)) {
                    psCT.setString(1, ct.getMaCTHD());
                    psCT.setString(2, hoaDon.getMaHD());
                    psCT.setString(3, ct.getMaBienThe());
                    psCT.setInt(4, ct.getSoLuong());
                    psCT.setLong(5, ct.getDonGia());
                    psCT.executeUpdate();
                }

                // A. Trừ kho theo công thức của Biến thể (Món chính)
                deductStockByFormula(conn, ct.getMaBienThe(), ct.getSoLuong());

                // B. Lưu Topping và trừ kho Topping
                for (Topping tp : ct.getToppings()) {
                    try (PreparedStatement psTP = conn.prepareStatement(sqlCTTopping)) {
                        psTP.setString(1, ct.getMaCTHD());
                        psTP.setString(2, tp.getMaTopping());
                        psTP.setLong(3, tp.getGiaBan());
                        psTP.executeUpdate();
                    }
                    // Trừ kho nguyên liệu liên kết với Topping
                    deductStockByTopping(conn, tp.getMaTopping(), ct.getSoLuong());
                }
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
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    private void deductStockByFormula(Connection conn, String maBienThe, int soLuongMon) throws SQLException {
        String sql = "SELECT MaNL, DinhLuong FROM CONG_THUC WHERE MaBienThe = ?";
        String sqlUpdate = "UPDATE NGUYEN_LIEU SET SoLuongTon = SoLuongTon - ? WHERE MaNL = ?";
        
        try (PreparedStatement psQuery = conn.prepareStatement(sql)) {
            psQuery.setString(1, maBienThe);
            try (ResultSet rs = psQuery.executeQuery()) {
                while (rs.next()) {
                    String maNL = rs.getString("MaNL");
                    double dinhLuong = rs.getDouble("DinhLuong");
                    double totalDeduct = dinhLuong * soLuongMon;
                    executeFefoDeduction(conn, maNL, totalDeduct);
                }
            }
        }
    }

    private void deductStockByTopping(Connection conn, String maTopping, int soLuongMon) throws SQLException {
        String sql = "SELECT MaNL_LienKet, DinhLuongHaoHut FROM TOPPING WHERE MaTopping = ?";
        String sqlUpdate = "UPDATE NGUYEN_LIEU SET SoLuongTon = SoLuongTon - ? WHERE MaNL = ?";

        try (PreparedStatement psQuery = conn.prepareStatement(sql)) {
            psQuery.setString(1, maTopping);
            try (ResultSet rs = psQuery.executeQuery()) {
                if (rs.next()) {
                    String maNL = rs.getString("MaNL_LienKet");
                    double haoHut = rs.getDouble("DinhLuongHaoHut");
                    if (maNL != null) {
                        double totalDeduct = haoHut * soLuongMon;
                        executeFefoDeduction(conn, maNL, totalDeduct);
                    }
                }
            }
        }
    }
    private void executeFefoDeduction(Connection conn, String maNL, double totalDeduct) throws SQLException {
        // 1. Trừ tổng kho hiển thị
        String sqlUpNL = "UPDATE NGUYEN_LIEU SET SoLuongTon = SoLuongTon - ? WHERE MaNL = ?";
        try (PreparedStatement psUp = conn.prepareStatement(sqlUpNL)) {
            psUp.setDouble(1, totalDeduct);
            psUp.setString(2, maNL);
            psUp.executeUpdate();
        }

        // 2. Thuật toán FEFO trừ kho theo Lô (Bỏ qua các lô đã hết hạn)
        String sqlSel = "SELECT MaLo, SoLuongConLai FROM LO_NGUYEN_LIEU WHERE MaNL = ? AND SoLuongConLai > 0 AND HanSuDung >= TRUNC(SYSDATE) ORDER BY HanSuDung ASC";
        String sqlUpLo = "UPDATE LO_NGUYEN_LIEU SET SoLuongConLai = ? WHERE MaLo = ?";
        double remainToDeduct = totalDeduct;

        try (PreparedStatement psSel = conn.prepareStatement(sqlSel)) {
            psSel.setString(1, maNL);
            try (ResultSet rs = psSel.executeQuery()) {
                while (rs.next() && remainToDeduct > 0) {
                    String maLo = rs.getString("MaLo");
                    double slLo = rs.getDouble("SoLuongConLai");

                    double slTru = Math.min(slLo, remainToDeduct);
                    double slConMoi = slLo - slTru;
                    remainToDeduct -= slTru;

                    try (PreparedStatement psUpLo = conn.prepareStatement(sqlUpLo)) {
                        psUpLo.setDouble(1, slConMoi);
                        psUpLo.setString(2, maLo);
                        psUpLo.executeUpdate();
                    }
                }
            }
        }
    }

    public String checkStockAvailability(HoaDon hoaDon) {
        // Hàm này sẽ kiểm tra xem với đơn hàng hiện tại, tồn kho có đủ không
        // Nếu không đủ, trả về chuỗi thông báo lỗi. Nếu đủ, trả về null.
        java.util.Map<String, Double> requiredNL = new java.util.HashMap<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            for (CTHoaDon ct : hoaDon.getChiTietHoaDon()) {
                // Tính nguyên liệu cho món chính
                String sqlCongThuc = "SELECT MaNL, DinhLuong FROM CONG_THUC WHERE MaBienThe = ?";
                try (PreparedStatement psCT = conn.prepareStatement(sqlCongThuc)) {
                    psCT.setString(1, ct.getMaBienThe());
                    try (ResultSet rs = psCT.executeQuery()) {
                        while (rs.next()) {
                            String maNL = rs.getString("MaNL");
                            double required = rs.getDouble("DinhLuong") * ct.getSoLuong();
                            requiredNL.put(maNL, requiredNL.getOrDefault(maNL, 0.0) + required);
                        }
                    }
                }
                
                // Tính nguyên liệu cho Topping
                for (Topping tp : ct.getToppings()) {
                    String sqlTopping = "SELECT MaNL_LienKet, DinhLuongHaoHut FROM TOPPING WHERE MaTopping = ?";
                    try (PreparedStatement psTP = conn.prepareStatement(sqlTopping)) {
                        psTP.setString(1, tp.getMaTopping());
                        try (ResultSet rs = psTP.executeQuery()) {
                            if (rs.next()) {
                                String maNL = rs.getString("MaNL_LienKet");
                                if (maNL != null) {
                                    double required = rs.getDouble("DinhLuongHaoHut") * ct.getSoLuong();
                                    requiredNL.put(maNL, requiredNL.getOrDefault(maNL, 0.0) + required);
                                }
                            }
                        }
                    }
                }
            }
            
            // Kiểm tra tổng tồn kho hiện tại
            StringBuilder warnings = new StringBuilder();
            String sqlCheck = "SELECT TenNL, SoLuongTon FROM NGUYEN_LIEU WHERE MaNL = ?";
            for (java.util.Map.Entry<String, Double> entry : requiredNL.entrySet()) {
                try (PreparedStatement psChk = conn.prepareStatement(sqlCheck)) {
                    psChk.setString(1, entry.getKey());
                    try (ResultSet rs = psChk.executeQuery()) {
                        if (rs.next()) {
                            double tonKho = rs.getDouble("SoLuongTon");
                            if (tonKho < entry.getValue()) {
                                warnings.append("- ").append(rs.getString("TenNL"))
                                        .append(" (Tồn: ").append(tonKho)
                                        .append(", Cần: ").append(entry.getValue()).append(")\n");
                            }
                        }
                    }
                }
            }
            
            if (warnings.length() > 0) {
                return "CẢNH BÁO: KHO KHÔNG ĐỦ NGUYÊN LIỆU!\n" + warnings.toString();
            }
            return null; // Đủ kho
        } catch (SQLException e) {
            e.printStackTrace();
            return "Lỗi kiểm tra tồn kho!";
        }
    }
}
