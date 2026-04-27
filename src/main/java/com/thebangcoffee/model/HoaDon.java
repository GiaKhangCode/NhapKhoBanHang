package com.thebangcoffee.model;

import java.util.Date;
import java.util.List;

public class HoaDon {
    private String maHD;
    private long tongTien;
    private String trangThai;
    private Date ngayTao;
    private List<CTHoaDon> chiTietHoaDon;

    public HoaDon() {}

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }
    public long getTongTien() { return tongTien; }
    public void setTongTien(long tongTien) { this.tongTien = tongTien; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }
    public List<CTHoaDon> getChiTietHoaDon() { return chiTietHoaDon; }
    public void setChiTietHoaDon(List<CTHoaDon> chiTietHoaDon) { this.chiTietHoaDon = chiTietHoaDon; }
}
