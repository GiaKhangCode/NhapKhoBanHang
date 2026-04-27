package com.thebangcoffee.model;

import java.util.Date;

public class ChiTietPhieuNhapKho {
    private String maPN;
    private String maNL;
    private String tenNL; // Transient field for UI display
    private double soLuong;
    private double donGia;
    private double tyLeQuyDoi = 1.0;
    private double thanhTien;
    private Date hanSuDung; // Hạn sử dụng của lô nhập này

    public ChiTietPhieuNhapKho() {
    }

    public ChiTietPhieuNhapKho(String maPN, String maNL, double soLuong, double donGia, double tyLeQuyDoi, double thanhTien, Date hanSuDung) {
        this.maPN = maPN;
        this.maNL = maNL;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.tyLeQuyDoi = tyLeQuyDoi;
        this.thanhTien = thanhTien;
        this.hanSuDung = hanSuDung;
    }

    public String getMaPN() { return maPN; }
    public void setMaPN(String maPN) { this.maPN = maPN; }

    public String getMaNL() { return maNL; }
    public void setMaNL(String maNL) { this.maNL = maNL; }
    
    public String getTenNL() { return tenNL; }
    public void setTenNL(String tenNL) { this.tenNL = tenNL; }

    public double getSoLuong() { return soLuong; }
    public void setSoLuong(double soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
    
    public double getTyLeQuyDoi() { return tyLeQuyDoi; }
    public void setTyLeQuyDoi(double tyLeQuyDoi) { this.tyLeQuyDoi = tyLeQuyDoi; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }

    public Date getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(Date hanSuDung) { this.hanSuDung = hanSuDung; }
}
