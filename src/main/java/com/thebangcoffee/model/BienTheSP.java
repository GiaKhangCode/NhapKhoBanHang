package com.thebangcoffee.model;

public class BienTheSP {
    private String maBienThe;
    private String maSP;
    private String tenSize;
    private long giaBan;
    private String tenSP; // Dùng để hiển thị UI

    public BienTheSP() {}

    public BienTheSP(String maBienThe, String maSP, String tenSize, long giaBan) {
        this.maBienThe = maBienThe;
        this.maSP = maSP;
        this.tenSize = tenSize;
        this.giaBan = giaBan;
    }

    public String getMaBienThe() { return maBienThe; }
    public void setMaBienThe(String maBienThe) { this.maBienThe = maBienThe; }
    public String getMaSP() { return maSP; }
    public void setMaSP(String maSP) { this.maSP = maSP; }
    public String getTenSize() { return tenSize; }
    public void setTenSize(String tenSize) { this.tenSize = tenSize; }
    public long getGiaBan() { return giaBan; }
    public void setGiaBan(long giaBan) { this.giaBan = giaBan; }
    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }
}
