package com.thebangcoffee.model;

public class Topping {
    private String maTopping;
    private String tenTopping;
    private long giaBan;
    private String maNLLienKet;
    private double dinhLuongHaoHut;

    public Topping() {}

    public Topping(String maTopping, String tenTopping, long giaBan, String maNLLienKet, double dinhLuongHaoHut) {
        this.maTopping = maTopping;
        this.tenTopping = tenTopping;
        this.giaBan = giaBan;
        this.maNLLienKet = maNLLienKet;
        this.dinhLuongHaoHut = dinhLuongHaoHut;
    }

    public String getMaTopping() { return maTopping; }
    public void setMaTopping(String maTopping) { this.maTopping = maTopping; }
    public String getTenTopping() { return tenTopping; }
    public void setTenTopping(String tenTopping) { this.tenTopping = tenTopping; }
    public long getGiaBan() { return giaBan; }
    public void setGiaBan(long giaBan) { this.giaBan = giaBan; }
    public String getMaNL_LienKet() { return maNLLienKet; }
    public void setMaNL_LienKet(String maNLLienKet) { this.maNLLienKet = maNLLienKet; }
    public double getDinhLuongHaoHut() { return dinhLuongHaoHut; }
    public void setDinhLuongHaoHut(double dinhLuongHaoHut) { this.dinhLuongHaoHut = dinhLuongHaoHut; }
}
