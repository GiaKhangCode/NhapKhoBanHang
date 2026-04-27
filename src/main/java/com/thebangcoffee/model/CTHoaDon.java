package com.thebangcoffee.model;

import java.util.ArrayList;
import java.util.List;

public class CTHoaDon {
    private String maCTHD;
    private String maHD;
    private String maBienThe;
    private int soLuong;
    private long donGia;
    private List<Topping> toppings; // Danh sách topping đi kèm món này

    public CTHoaDon() {
        this.toppings = new ArrayList<>();
    }

    public String getMaCTHD() { return maCTHD; }
    public void setMaCTHD(String maCTHD) { this.maCTHD = maCTHD; }
    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }
    public String getMaBienThe() { return maBienThe; }
    public void setMaBienThe(String maBienThe) { this.maBienThe = maBienThe; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public long getDonGia() { return donGia; }
    public void setDonGia(long donGia) { this.donGia = donGia; }
    public List<Topping> getToppings() { return toppings; }
    public void setToppings(List<Topping> toppings) { this.toppings = toppings; }
}
