package com.thebangcoffee.model;

import java.util.ArrayList;
import java.util.List;

public class SanPham {
    private String maSP;
    private String tenSP;
    private List<BienTheSP> danhSachBienThe;
    private List<String> allowedToppings;

    public SanPham() {
        this.danhSachBienThe = new ArrayList<>();
        this.allowedToppings = new ArrayList<>();
    }

    public SanPham(String maSP, String tenSP) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.danhSachBienThe = new ArrayList<>();
        this.allowedToppings = new ArrayList<>();
    }

    public String getMaSP() { return maSP; }
    public void setMaSP(String maSP) { this.maSP = maSP; }
    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }
    public List<BienTheSP> getDanhSachBienThe() { return danhSachBienThe; }
    public void setDanhSachBienThe(List<BienTheSP> danhSachBienThe) { this.danhSachBienThe = danhSachBienThe; }
    public List<String> getAllowedToppings() { return allowedToppings; }
    public void setAllowedToppings(List<String> allowedToppings) { this.allowedToppings = allowedToppings; }
}
