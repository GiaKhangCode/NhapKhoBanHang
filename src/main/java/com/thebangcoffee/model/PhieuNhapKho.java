package com.thebangcoffee.model;

import java.util.Date;
import java.util.List;

public class PhieuNhapKho {
    private String maPN;
    private Date ngayNhap;
    private double tongTien;
    private String ghiChu;
    private List<ChiTietPhieuNhapKho> danhSachChiTiet;

    public PhieuNhapKho() {
    }

    public PhieuNhapKho(String maPN, Date ngayNhap, double tongTien, String ghiChu) {
        this.maPN = maPN;
        this.ngayNhap = ngayNhap;
        this.tongTien = tongTien;
        this.ghiChu = ghiChu;
    }

    public String getMaPN() { return maPN; }
    public void setMaPN(String maPN) { this.maPN = maPN; }

    public Date getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(Date ngayNhap) { this.ngayNhap = ngayNhap; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public List<ChiTietPhieuNhapKho> getDanhSachChiTiet() { return danhSachChiTiet; }
    public void setDanhSachChiTiet(List<ChiTietPhieuNhapKho> danhSachChiTiet) { this.danhSachChiTiet = danhSachChiTiet; }
}
