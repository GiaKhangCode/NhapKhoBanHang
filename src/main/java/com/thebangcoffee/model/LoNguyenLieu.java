package com.thebangcoffee.model;

import java.util.Date;

public class LoNguyenLieu {
    private String maLo;
    private String maNL;
    private String maPN;
    private double soLuongBanDau;
    private double soLuongConLai;
    private double donGiaQuyDoi;
    private Date hanSuDung;

    public LoNguyenLieu() {}

    public LoNguyenLieu(String maLo, String maNL, String maPN, double soLuongBanDau, double soLuongConLai, double donGiaQuyDoi, Date hanSuDung) {
        this.maLo = maLo;
        this.maNL = maNL;
        this.maPN = maPN;
        this.soLuongBanDau = soLuongBanDau;
        this.soLuongConLai = soLuongConLai;
        this.donGiaQuyDoi = donGiaQuyDoi;
        this.hanSuDung = hanSuDung;
    }

    public String getMaPN() { return maPN; }
    public void setMaPN(String maPN) { this.maPN = maPN; }
    
    public double getDonGiaQuyDoi() { return donGiaQuyDoi; }
    public void setDonGiaQuyDoi(double donGiaQuyDoi) { this.donGiaQuyDoi = donGiaQuyDoi; }

    public String getMaLo() { return maLo; }
    public void setMaLo(String maLo) { this.maLo = maLo; }

    public String getMaNL() { return maNL; }
    public void setMaNL(String maNL) { this.maNL = maNL; }

    public double getSoLuongBanDau() { return soLuongBanDau; }
    public void setSoLuongBanDau(double soLuongBanDau) { this.soLuongBanDau = soLuongBanDau; }

    public double getSoLuongConLai() { return soLuongConLai; }
    public void setSoLuongConLai(double soLuongConLai) { this.soLuongConLai = soLuongConLai; }

    public Date getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(Date hanSuDung) { this.hanSuDung = hanSuDung; }
}
