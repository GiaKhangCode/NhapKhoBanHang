package com.thebangcoffee.model;

public class NguyenLieu {
    private String maNL;
    private String tenNL;
    private String dvtCoBan;
    private double soLuongTon;

    public NguyenLieu() {}

    public NguyenLieu(String maNL, String tenNL, String dvtCoBan, double soLuongTon) {
        this.maNL = maNL;
        this.tenNL = tenNL;
        this.dvtCoBan = dvtCoBan;
        this.soLuongTon = soLuongTon;
    }

    public String getMaNL() { return maNL; }
    public void setMaNL(String maNL) { this.maNL = maNL; }
    public String getTenNL() { return tenNL; }
    public void setTenNL(String tenNL) { this.tenNL = tenNL; }
    public String getDvtCoBan() { return dvtCoBan; }
    public void setDvtCoBan(String dvtCoBan) { this.dvtCoBan = dvtCoBan; }
    public double getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(double soLuongTon) { this.soLuongTon = soLuongTon; }

    @Override
    public String toString() {
        return tenNL + " (" + dvtCoBan + ")";
    }
}
