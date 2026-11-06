package entity;

import java.sql.Timestamp;
import java.util.Objects;

public class PhieuTiepNhan {
    private String maPhieuTiepNhan;
    private Xe xe;
    private KhachHang khachHang;
    private NhanVien nhanVien;
    private Timestamp ngayLap;
    private String trangThai;
    private String ghiChu;

    public PhieuTiepNhan() {
    }

    public PhieuTiepNhan(String maPhieuTiepNhan) {
        this.maPhieuTiepNhan = maPhieuTiepNhan;
    }

    public PhieuTiepNhan(String maPhieuTiepNhan, Xe xe, KhachHang khachHang, NhanVien nhanVien,
            Timestamp ngayLap, String trangThai, String ghiChu) {
        this.maPhieuTiepNhan = maPhieuTiepNhan;
        this.xe = xe;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
        this.ngayLap = ngayLap;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public String getMaPhieuTiepNhan() { return maPhieuTiepNhan; }
    public void setMaPhieuTiepNhan(String maPhieuTiepNhan) { this.maPhieuTiepNhan = maPhieuTiepNhan; }
    public Xe getXe() { return xe; }
    public void setXe(Xe xe) { this.xe = xe; }
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public Timestamp getNgayLap() { return ngayLap; }
    public void setNgayLap(Timestamp ngayLap) { this.ngayLap = ngayLap; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    @Override
    public String toString() {
        return maPhieuTiepNhan + " - "
                + (xe == null ? "" : xe.getBienSo() == null ? xe.getMaXe() : xe.getBienSo())
                + " (" + trangThai + ")";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PhieuTiepNhan other)) return false;
        return maPhieuTiepNhan != null && maPhieuTiepNhan.equals(other.maPhieuTiepNhan);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maPhieuTiepNhan);
    }
}
