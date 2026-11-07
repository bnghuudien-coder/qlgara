package controller;

import dao.DichVuDAO;
import entity.DichVu;
import gui.FrmDichVu;

import java.sql.SQLException;

public final class DichVuController {
    private final FrmDichVu view;
    private final DichVuDAO dichVuDAO = new DichVuDAO();

    public DichVuController(FrmDichVu view) {
        this.view = view;
        dangKySuKien();
    }

    public void taiDuLieuBanDau() {
        taiDanhSachDichVu();
        lamMoiForm();
    }

    private void dangKySuKien() {
        view.layNutThem().addActionListener(event -> themDichVu());
        view.layNutCapNhat().addActionListener(event -> capNhatDichVu());
        view.layNutXoa().addActionListener(event -> xoaDichVu());
        view.layNutLamMoi().addActionListener(event -> lamMoiForm());
        view.layNutTaiLai().addActionListener(event -> {
            view.xoaBoLoc();
            taiDanhSachDichVu();
        });
    }

    private void taiDanhSachDichVu() {
        try {
            view.hienThiDanhSach(dichVuDAO.findAll());
            view.thongBao("Đã tải " + view.soLuongDichVu() + " dịch vụ");
        } catch (SQLException exception) {
            view.baoLoi("Không thể tải danh sách dịch vụ", exception.getMessage());
        }
    }

    private void themDichVu() {
        try {
            DichVu dichVu = view.layDichVuNhap();
            if (dichVuDAO.them(dichVu)) {
                taiDanhSachDichVu();
                lamMoiForm();
                view.thongBaoThanhCong("Đã thêm dịch vụ " + dichVu.getMaDV());
            }
        } catch (IllegalArgumentException exception) {
            view.baoLoi("Dữ liệu dịch vụ không hợp lệ", exception.getMessage());
        } catch (SQLException exception) {
            view.baoLoi("Không thể thêm dịch vụ", exception.getMessage());
        }
    }

    private void capNhatDichVu() {
        try {
            DichVu dichVu = view.layDichVuNhap();
            if (!dichVuDAO.capNhat(dichVu)) {
                view.baoLoi("Không tìm thấy dịch vụ cần cập nhật", null);
                return;
            }
            taiDanhSachDichVu();
            view.thongBaoThanhCong("Đã cập nhật dịch vụ " + dichVu.getMaDV());
        } catch (IllegalArgumentException exception) {
            view.baoLoi("Dữ liệu dịch vụ không hợp lệ", exception.getMessage());
        } catch (SQLException exception) {
            view.baoLoi("Không thể cập nhật dịch vụ", exception.getMessage());
        }
    }

    private void xoaDichVu() {
        String maDV = view.layMaDichVuNhap();
        if (!view.xacNhanXoa(maDV)) {
            return;
        }
        try {
            if (!dichVuDAO.xoa(maDV)) {
                view.baoLoi("Không tìm thấy dịch vụ cần xóa", null);
                return;
            }
            taiDanhSachDichVu();
            lamMoiForm();
            view.thongBaoThanhCong("Đã xóa dịch vụ " + maDV);
        } catch (SQLException exception) {
            view.baoLoi("Không thể xóa dịch vụ", exception.getMessage());
        }
    }

    private void lamMoiForm() {
        try {
            view.lamMoiForm(dichVuDAO.layMaTiepTheo());
        } catch (SQLException exception) {
            view.baoLoi("Không thể tạo mã dịch vụ", exception.getMessage());
            view.lamMoiForm("");
        }
    }
}