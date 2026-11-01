package entity;

import java.sql.Timestamp;

public class PhieuYeuCauBaoHanh {
    private String maPhieuYCBH;
    private String maHoaDon;
    private String maPhuTung;
    private int soLuong;
    private Timestamp ngayTiepNhan;
    private String trangThai;
    private String lyDo;
    private String ghiChuXuLy;
    private Timestamp ngayXuLy;
    private Timestamp ngayHoanThanh;

    public String getMaPhieuYCBH() { return maPhieuYCBH; }
    public void setMaPhieuYCBH(String maPhieuYCBH) { this.maPhieuYCBH = maPhieuYCBH; }
    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public String getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(String maPhuTung) { this.maPhuTung = maPhuTung; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public Timestamp getNgayTiepNhan() { return ngayTiepNhan; }
    public void setNgayTiepNhan(Timestamp ngayTiepNhan) { this.ngayTiepNhan = ngayTiepNhan; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
    public String getGhiChuXuLy() { return ghiChuXuLy; }
    public void setGhiChuXuLy(String ghiChuXuLy) { this.ghiChuXuLy = ghiChuXuLy; }
    public Timestamp getNgayXuLy() { return ngayXuLy; }
    public void setNgayXuLy(Timestamp ngayXuLy) { this.ngayXuLy = ngayXuLy; }
    public Timestamp getNgayHoanThanh() { return ngayHoanThanh; }
    public void setNgayHoanThanh(Timestamp ngayHoanThanh) { this.ngayHoanThanh = ngayHoanThanh; }
}
