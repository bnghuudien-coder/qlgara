package entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ChinhSachBaoHanh {
    private String maCSBH;
    private String tenChinhSach;
    private int thoiHanBaoHanh;
    private String donViThoiGian;
    private String dieuKienBaoHanh;
    private String truongHopTuChoi;
    private String noiDungBaoHanh;
    private Timestamp ngayTao;
    private boolean trangThai;
    private List<String> dsMaPhuTung = new ArrayList<>();

    public String getMaCSBH() { return maCSBH; }
    public void setMaCSBH(String maCSBH) { this.maCSBH = maCSBH; }
    public String getTenChinhSach() { return tenChinhSach; }
    public void setTenChinhSach(String tenChinhSach) { this.tenChinhSach = tenChinhSach; }
    public int getThoiHanBaoHanh() { return thoiHanBaoHanh; }
    public void setThoiHanBaoHanh(int thoiHanBaoHanh) { this.thoiHanBaoHanh = thoiHanBaoHanh; }
    public String getDonViThoiGian() { return donViThoiGian; }
    public void setDonViThoiGian(String donViThoiGian) { this.donViThoiGian = donViThoiGian; }
    public String getDieuKienBaoHanh() { return dieuKienBaoHanh; }
    public void setDieuKienBaoHanh(String dieuKienBaoHanh) { this.dieuKienBaoHanh = dieuKienBaoHanh; }
    public String getTruongHopTuChoi() { return truongHopTuChoi; }
    public void setTruongHopTuChoi(String truongHopTuChoi) { this.truongHopTuChoi = truongHopTuChoi; }
    public String getNoiDungBaoHanh() { return noiDungBaoHanh; }
    public void setNoiDungBaoHanh(String noiDungBaoHanh) { this.noiDungBaoHanh = noiDungBaoHanh; }
    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    public List<String> getDsMaPhuTung() { return new ArrayList<>(dsMaPhuTung); }
    public void setDsMaPhuTung(List<String> dsMaPhuTung) {
        this.dsMaPhuTung = dsMaPhuTung == null ? new ArrayList<>() : new ArrayList<>(dsMaPhuTung);
    }
}
