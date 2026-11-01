package gui;

import dao.PhieuYeuCauBaoHanhDAO;
import dao.PhieuYeuCauBaoHanhDAO.LuaChonHoaDon;
import dao.PhieuYeuCauBaoHanhDAO.LuaChonPhuTung;
import dao.PhieuYeuCauBaoHanhDAO.YeuCauHienThi;
import entity.PhieuYeuCauBaoHanh;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

public class FrmTiepNhanBaoHanh extends JPanel {
    private static final DateTimeFormatter DINH_DANG_NGAY_GIO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PhieuYeuCauBaoHanhDAO phieuYeuCauBaoHanhDAO = new PhieuYeuCauBaoHanhDAO();
    private final AppTextField txtMaPhieuYCBH = new AppTextField(14);
    private final JComboBox<LuaChonHoaDon> cboHoaDon = new JComboBox<>();
    private final JComboBox<LuaChonPhuTung> cboPhuTung = new JComboBox<>();
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
    private final JTextArea txaLyDo = taoOThongTin();
    private final JTextArea txaGhiChuXuLy = taoOThongTin();
    private final AppTextField txtTimKiem = new AppTextField(20);
    private final JLabel lblThongBao = new AppLabel("Yêu cầu chờ xử lý được ưu tiên hiển thị trước.", AppLabel.Style.MUTED);
    private final List<YeuCauHienThi> dsYeuCau = new ArrayList<>();
    private boolean dangNapHoaDon;
    private boolean dangTaoMoi = true;
    private final AppButton btnThem = new AppButton("Tiếp nhận");
    private final AppButton btnCapNhat = new AppButton("Cập nhật", AppButton.Style.SUCCESS);
    private final AppButton btnXoa = new AppButton("Xóa", AppButton.Style.DANGER);
    private final AppButton btnChapNhan = new AppButton("Chấp nhận bảo hành", AppButton.Style.SUCCESS);
    private final AppButton btnHoanThanh = new AppButton("Hoàn thành", AppButton.Style.PRIMARY);
    private final AppButton btnTuChoi = new AppButton("Từ chối", AppButton.Style.DANGER);
    private final AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);

    private final DefaultTableModel modelYeuCau = new DefaultTableModel(
            new Object[] { "Mã yêu cầu", "Hóa đơn", "Phụ tùng", "Khách hàng", "SL", "Ngày tiếp nhận", "Trạng thái" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblYeuCau = FormUI.createTable(modelYeuCau);

    public FrmTiepNhanBaoHanh() {
        setLayout(new BorderLayout());
        setOpaque(false);
        txtMaPhieuYCBH.setEditable(false);
        taiMaYeuCauMoi();
        tblYeuCau.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblYeuCau.setAutoCreateRowSorter(false);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoKhungThongTin(), BorderLayout.NORTH);
        pnlNoiDung.add(taoBangYeuCau(), BorderLayout.CENTER);
        add(FormUI.createPage("TIẾP NHẬN BẢO HÀNH",
                "Tiếp nhận, xử lý và theo dõi các yêu cầu bảo hành phụ tùng", pnlNoiDung),
                BorderLayout.CENTER);

        cboHoaDon.addActionListener(event -> {
            if (!dangNapHoaDon) {
                taiPhuTungTheoHoaDon();
            }
        });
        cboPhuTung.addActionListener(event -> capNhatGioiHanSoLuong());
        tblYeuCau.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tblYeuCau.getSelectedRow() >= 0) {
                hienThiYeuCauDangChon();
            }
        });
        txtTimKiem.addActionListener(event -> taiDanhSachYeuCau());
        capNhatTrangThaiNut(false, false);
        taiDanhSachHoaDon();
        taiDanhSachYeuCau();
    }

    private JPanel taoKhungThongTin() {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.add(FormUI.createSectionTitle("Thông tin yêu cầu bảo hành"), BorderLayout.NORTH);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(12, 0));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoCacTruong(), BorderLayout.CENTER);
        pnlNoiDung.add(taoGhiChu(), BorderLayout.EAST);
        pnlCard.add(pnlNoiDung, BorderLayout.CENTER);
        return pnlCard;
    }

    private JPanel taoCacTruong() {
        JPanel pnlTruong = FormUI.createFieldsPanel();
        FormUI.addField(pnlTruong, "Mã yêu cầu", txtMaPhieuYCBH, 0, 0);
        FormUI.addField(pnlTruong, "Hóa đơn *", cboHoaDon, 2, 0);
        FormUI.addField(pnlTruong, "Phụ tùng *", cboPhuTung, 0, 1);
        FormUI.addField(pnlTruong, "Số lượng *", spnSoLuong, 2, 1);
        FormUI.addField(pnlTruong, "Lý do yêu cầu *", new AppScrollPane(txaLyDo), 0, 2);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 0, 5, 18);
        JLabel lblGhiChu = new AppLabel("Chỉ nhận phụ tùng còn thời hạn bảo hành.", AppLabel.Style.MUTED);
        pnlTruong.add(lblGhiChu, gbc);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 10));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(pnlTruong, BorderLayout.CENTER);
        pnlNoiDung.add(taoNutChucNang(), BorderLayout.SOUTH);
        return pnlNoiDung;
    }

    private JPanel taoGhiChu() {
        JPanel pnlGhiChu = new JPanel(new BorderLayout(0, 5));
        pnlGhiChu.setOpaque(false);
        pnlGhiChu.setPreferredSize(new Dimension(300, 180));
        pnlGhiChu.add(new AppLabel("Ghi chú xử lý"), BorderLayout.NORTH);
        pnlGhiChu.add(new AppScrollPane(txaGhiChuXuLy), BorderLayout.CENTER);
        return pnlGhiChu;
    }

    private JPanel taoNutChucNang() {
        btnThem.addActionListener(event -> themYeuCau());
        btnCapNhat.addActionListener(event -> capNhatYeuCau());
        btnXoa.addActionListener(event -> xoaYeuCau());
        btnChapNhan.addActionListener(event -> chapNhanYeuCau());
        btnHoanThanh.addActionListener(event -> hoanThanhYeuCau());
        btnTuChoi.addActionListener(event -> tuChoiYeuCau());
        btnLamMoi.addActionListener(event -> lamMoiForm());
        return FormUI.createButtonRow(
                btnThem, btnCapNhat, btnXoa, btnChapNhan, btnHoanThanh, btnTuChoi, btnLamMoi);
    }

    private JPanel taoBangYeuCau() {
        AppPanel pnlBang = FormUI.createCard();
        JPanel pnlDau = new JPanel(new BorderLayout(10, 0));
        pnlDau.setOpaque(false);
        pnlDau.add(FormUI.createSectionTitle("Danh sách yêu cầu bảo hành"), BorderLayout.WEST);
        txtTimKiem.setToolTipText("Tìm mã yêu cầu, hóa đơn, phụ tùng, khách hàng hoặc trạng thái");
        AppButton btnTimKiem = new AppButton("Tìm kiếm");
        AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);
        btnTimKiem.addActionListener(event -> taiDanhSachYeuCau());
        btnTaiLai.addActionListener(event -> {
            txtTimKiem.setText("");
            taiDanhSachHoaDon();
            taiDanhSachYeuCau();
        });
        JPanel pnlTimKiem = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlTimKiem.setOpaque(false);
        pnlTimKiem.add(txtTimKiem);
        pnlTimKiem.add(btnTimKiem);
        pnlTimKiem.add(btnTaiLai);
        pnlDau.add(pnlTimKiem, BorderLayout.EAST);
        pnlBang.add(pnlDau, BorderLayout.NORTH);
        pnlBang.add(new AppScrollPane(tblYeuCau), BorderLayout.CENTER);
        pnlBang.add(lblThongBao, BorderLayout.SOUTH);
        return pnlBang;
    }

    private void taiDanhSachHoaDon() {
        dangNapHoaDon = true;
        cboHoaDon.removeAllItems();
        cboPhuTung.removeAllItems();
        try {
            for (LuaChonHoaDon hoaDon : phieuYeuCauBaoHanhDAO.layHoaDonConHanBaoHanh()) {
                cboHoaDon.addItem(hoaDon);
            }
        } catch (SQLException exception) {
            baoLoi("Không thể tải hóa đơn còn hạn bảo hành", exception);
        } finally {
            dangNapHoaDon = false;
        }
        taiPhuTungTheoHoaDon();
    }

    private void taiPhuTungTheoHoaDon() {
        cboPhuTung.removeAllItems();
        LuaChonHoaDon hoaDon = (LuaChonHoaDon) cboHoaDon.getSelectedItem();
        if (hoaDon == null) {
            return;
        }
        try {
            for (LuaChonPhuTung phuTung : phieuYeuCauBaoHanhDAO
                    .layPhuTungConHanTheoHoaDon(hoaDon.maHoaDon())) {
                cboPhuTung.addItem(phuTung);
            }
            capNhatGioiHanSoLuong();
        } catch (SQLException exception) {
            baoLoi("Không thể tải phụ tùng được bảo hành của hóa đơn", exception);
        }
    }

    private void capNhatGioiHanSoLuong() {
        LuaChonPhuTung phuTung = (LuaChonPhuTung) cboPhuTung.getSelectedItem();
        int gioiHan = phuTung == null ? 1 : Math.max(1, phuTung.soLuongHoaDon());
        spnSoLuong.setModel(new SpinnerNumberModel(1, 1, gioiHan, 1));
    }

    private void taiDanhSachYeuCau() {
        modelYeuCau.setRowCount(0);
        dsYeuCau.clear();
        try {
            dsYeuCau.addAll(phieuYeuCauBaoHanhDAO.timKiem(txtTimKiem.getText()));
            for (YeuCauHienThi yeuCau : dsYeuCau) {
                modelYeuCau.addRow(new Object[] {
                        yeuCau.maPhieuYCBH(),
                        yeuCau.maHoaDon(),
                        yeuCau.maPhuTung() + " - " + yeuCau.tenPhuTung(),
                        yeuCau.tenKhachHang(),
                        yeuCau.soLuong(),
                        dinhDangNgay(yeuCau.ngayTiepNhan()),
                        yeuCau.trangThai()
                });
            }
            lblThongBao.setText("Thứ tự: Chờ xử lý → Chấp nhận bảo hành → Hoàn thành bảo hành → Từ chối bảo hành.");
        } catch (SQLException exception) {
            baoLoi("Không thể tải danh sách yêu cầu bảo hành", exception);
        }
    }

    private void hienThiYeuCauDangChon() {
        int row = tblYeuCau.getSelectedRow();
        if (row < 0 || row >= dsYeuCau.size()) {
            return;
        }
        YeuCauHienThi yeuCau = dsYeuCau.get(row);
        dangTaoMoi = false;
        txtMaPhieuYCBH.setText(yeuCau.maPhieuYCBH());
        chonHoaDonHienTai(yeuCau);
        LuaChonPhuTung selectedPart = null;
        for (int i = 0; i < cboPhuTung.getItemCount(); i++) {
            if (cboPhuTung.getItemAt(i).maPhuTung().equals(yeuCau.maPhuTung())) {
                selectedPart = cboPhuTung.getItemAt(i);
                break;
            }
        }
        if (selectedPart == null) {
            selectedPart = new LuaChonPhuTung(
                    yeuCau.maPhuTung(), yeuCau.tenPhuTung(), yeuCau.soLuong(), 0, "");
            cboPhuTung.addItem(selectedPart);
        }
        cboPhuTung.setSelectedItem(selectedPart);
        spnSoLuong.setValue(Math.max(1, yeuCau.soLuong()));
        txaLyDo.setText(yeuCau.lyDo() == null ? "" : yeuCau.lyDo());
        txaGhiChuXuLy.setText(yeuCau.ghiChuXuLy() == null ? "" : yeuCau.ghiChuXuLy());
        boolean choXuLy = PhieuYeuCauBaoHanhDAO.CHO_XU_LY.equals(yeuCau.trangThai());
        boolean daChapNhan = PhieuYeuCauBaoHanhDAO.CHAP_NHAN.equals(yeuCau.trangThai());
        cboHoaDon.setEnabled(false);
        cboPhuTung.setEnabled(false);
        spnSoLuong.setEnabled(choXuLy);
        txaLyDo.setEditable(choXuLy);
        lblThongBao.setText("Trạng thái: " + yeuCau.trangThai()
                + (yeuCau.ngayXuLy() == null ? "" : " | Xử lý: " + dinhDangNgay(yeuCau.ngayXuLy()))
                + (yeuCau.ngayHoanThanh() == null ? "" : " | Hoàn thành: " + dinhDangNgay(yeuCau.ngayHoanThanh())));
        capNhatTrangThaiNut(choXuLy, daChapNhan);
    }

    private void chonHoaDonHienTai(YeuCauHienThi yeuCau) {
        for (int i = 0; i < cboHoaDon.getItemCount(); i++) {
            if (cboHoaDon.getItemAt(i).maHoaDon().equals(yeuCau.maHoaDon())) {
                cboHoaDon.setSelectedIndex(i);
                return;
            }
        }
        LuaChonHoaDon hoaDon = new LuaChonHoaDon(yeuCau.maHoaDon(),
                yeuCau.tenKhachHang() == null ? "Khách hàng" : yeuCau.tenKhachHang(), null);
        cboHoaDon.addItem(hoaDon);
        cboHoaDon.setSelectedItem(hoaDon);
    }

    private void themYeuCau() {
        LuaChonHoaDon hoaDon = (LuaChonHoaDon) cboHoaDon.getSelectedItem();
        LuaChonPhuTung phuTung = (LuaChonPhuTung) cboPhuTung.getSelectedItem();
        if (hoaDon == null || phuTung == null) {
            thongBao("Vui lòng chọn hóa đơn và phụ tùng còn hạn bảo hành.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (txaLyDo.getText().isBlank()) {
            thongBao("Vui lòng nhập lý do yêu cầu bảo hành.", JOptionPane.WARNING_MESSAGE);
            txaLyDo.requestFocusInWindow();
            return;
        }
        PhieuYeuCauBaoHanh yeuCau = taoYeuCau();
        yeuCau.setMaHoaDon(hoaDon.maHoaDon());
        yeuCau.setMaPhuTung(phuTung.maPhuTung());
        try {
            phieuYeuCauBaoHanhDAO.them(yeuCau);
            txtTimKiem.setText("");
            taiDanhSachYeuCau();
            chonYeuCau(yeuCau.getMaPhieuYCBH());
            thongBao("Đã tiếp nhận yêu cầu " + yeuCau.getMaPhieuYCBH()
                    + " với trạng thái Chờ xử lý.", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            baoLoi("Không thể tiếp nhận yêu cầu bảo hành", exception);
        }
    }

    private void capNhatYeuCau() {
        YeuCauHienThi yeuCauDangChon = layYeuCauDangChon();
        if (yeuCauDangChon == null) {
            return;
        }
        if (!PhieuYeuCauBaoHanhDAO.CHO_XU_LY.equals(yeuCauDangChon.trangThai())) {
            thongBao("Chỉ có thể cập nhật yêu cầu đang chờ xử lý.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (txaLyDo.getText().isBlank()) {
            thongBao("Vui lòng nhập lý do yêu cầu bảo hành.", JOptionPane.WARNING_MESSAGE);
            txaLyDo.requestFocusInWindow();
            return;
        }
        PhieuYeuCauBaoHanh yeuCau = taoYeuCau();
        yeuCau.setMaPhieuYCBH(yeuCauDangChon.maPhieuYCBH());
        try {
            phieuYeuCauBaoHanhDAO.capNhatChoXuLy(yeuCau);
            taiDanhSachYeuCau();
            chonYeuCau(yeuCau.getMaPhieuYCBH());
            thongBao("Đã cập nhật yêu cầu bảo hành.", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            baoLoi("Không thể cập nhật yêu cầu bảo hành", exception);
        }
    }

    private void xoaYeuCau() {
        YeuCauHienThi yeuCau = layYeuCauDangChon();
        if (yeuCau == null) {
            return;
        }
        if (!PhieuYeuCauBaoHanhDAO.CHO_XU_LY.equals(yeuCau.trangThai())) {
            thongBao("Chỉ có thể xóa yêu cầu đang chờ xử lý.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int luaChon = JOptionPane.showConfirmDialog(this,
                "Xóa yêu cầu " + yeuCau.maPhieuYCBH() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (!phieuYeuCauBaoHanhDAO.xoaChoXuLy(yeuCau.maPhieuYCBH())) {
                throw new SQLException("Yêu cầu đã được xử lý hoặc không còn tồn tại.");
            }
            lamMoiForm();
            taiDanhSachYeuCau();
        } catch (SQLException exception) {
            baoLoi("Không thể xóa yêu cầu bảo hành", exception);
        }
    }

    private void chapNhanYeuCau() {
        YeuCauHienThi yeuCau = layYeuCauDangChon();
        if (yeuCau == null || !xacNhanTrangThai(yeuCau, PhieuYeuCauBaoHanhDAO.CHO_XU_LY,
                "Chấp nhận yêu cầu này? Số lượng phụ tùng sẽ được cộng vào tồn kho.")) {
            return;
        }
        try {
            phieuYeuCauBaoHanhDAO.chapNhan(yeuCau.maPhieuYCBH(), txaGhiChuXuLy.getText().trim());
            taiDanhSachYeuCau();
            chonYeuCau(yeuCau.maPhieuYCBH());
            thongBao("Đã chấp nhận bảo hành và cập nhật tồn kho.", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            baoLoi("Không thể chấp nhận yêu cầu bảo hành", exception);
        }
    }

    private void tuChoiYeuCau() {
        YeuCauHienThi yeuCau = layYeuCauDangChon();
        if (yeuCau == null || !xacNhanTrangThai(
                yeuCau, PhieuYeuCauBaoHanhDAO.CHO_XU_LY, "Từ chối yêu cầu bảo hành này?")) {
            return;
        }
        if (txaGhiChuXuLy.getText().isBlank()) {
            thongBao("Vui lòng ghi lý do từ chối trong phần Ghi chú xử lý.", JOptionPane.WARNING_MESSAGE);
            txaGhiChuXuLy.requestFocusInWindow();
            return;
        }
        try {
            phieuYeuCauBaoHanhDAO.tuChoi(yeuCau.maPhieuYCBH(), txaGhiChuXuLy.getText().trim());
            taiDanhSachYeuCau();
            chonYeuCau(yeuCau.maPhieuYCBH());
            thongBao("Đã từ chối yêu cầu bảo hành.", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            baoLoi("Không thể từ chối yêu cầu bảo hành", exception);
        }
    }

    private void hoanThanhYeuCau() {
        YeuCauHienThi yeuCau = layYeuCauDangChon();
        if (yeuCau == null || !xacNhanTrangThai(yeuCau, PhieuYeuCauBaoHanhDAO.CHAP_NHAN,
                "Đánh dấu yêu cầu này đã hoàn thành bảo hành?")) {
            return;
        }
        if (txaGhiChuXuLy.getText().isBlank()) {
            thongBao("Vui lòng nhập ghi chú kết quả bảo hành.", JOptionPane.WARNING_MESSAGE);
            txaGhiChuXuLy.requestFocusInWindow();
            return;
        }
        try {
            phieuYeuCauBaoHanhDAO.hoanThanh(yeuCau.maPhieuYCBH(), txaGhiChuXuLy.getText().trim());
            taiDanhSachYeuCau();
            chonYeuCau(yeuCau.maPhieuYCBH());
            thongBao("Đã hoàn thành yêu cầu bảo hành.", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            baoLoi("Không thể hoàn thành yêu cầu bảo hành", exception);
        }
    }

    private boolean xacNhanTrangThai(YeuCauHienThi yeuCau, String trangThaiCanCo, String loiNhac) {
        if (!trangThaiCanCo.equals(yeuCau.trangThai())) {
            thongBao("Yêu cầu không ở trạng thái phù hợp để thực hiện thao tác này.",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return JOptionPane.showConfirmDialog(this, loiNhac, "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private PhieuYeuCauBaoHanh taoYeuCau() {
        PhieuYeuCauBaoHanh yeuCau = new PhieuYeuCauBaoHanh();
        yeuCau.setSoLuong((Integer) spnSoLuong.getValue());
        yeuCau.setLyDo(txaLyDo.getText().trim());
        yeuCau.setGhiChuXuLy(txaGhiChuXuLy.getText().trim());
        return yeuCau;
    }

    private YeuCauHienThi layYeuCauDangChon() {
        int row = tblYeuCau.getSelectedRow();
        if (row < 0 || row >= dsYeuCau.size()) {
            thongBao("Vui lòng chọn một yêu cầu bảo hành.", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return dsYeuCau.get(row);
    }

    private void chonYeuCau(String maPhieuYCBH) {
        for (int row = 0; row < dsYeuCau.size(); row++) {
            if (maPhieuYCBH.equals(dsYeuCau.get(row).maPhieuYCBH())) {
                tblYeuCau.setRowSelectionInterval(row, row);
                tblYeuCau.scrollRectToVisible(tblYeuCau.getCellRect(row, 0, true));
                return;
            }
        }
    }

    private void lamMoiForm() {
        tblYeuCau.clearSelection();
        dangTaoMoi = true;
        taiMaYeuCauMoi();
        cboHoaDon.setEnabled(true);
        cboPhuTung.setEnabled(true);
        spnSoLuong.setEnabled(true);
        txaLyDo.setEditable(true);
        spnSoLuong.setValue(1);
        txaLyDo.setText("");
        txaGhiChuXuLy.setText("");
        capNhatTrangThaiNut(false, false);
        taiDanhSachHoaDon();
    }

    private void capNhatTrangThaiNut(boolean choXuLy, boolean daChapNhan) {
        btnThem.setEnabled(dangTaoMoi);
        btnCapNhat.setEnabled(choXuLy);
        btnXoa.setEnabled(choXuLy);
        btnChapNhan.setEnabled(choXuLy);
        btnTuChoi.setEnabled(choXuLy);
        btnHoanThanh.setEnabled(daChapNhan);
    }

    private void taiMaYeuCauMoi() {
        try {
            txtMaPhieuYCBH.setText(phieuYeuCauBaoHanhDAO.layMaTiepTheo());
        } catch (SQLException exception) {
            txtMaPhieuYCBH.setText("");
            baoLoi("Không thể tạo mã yêu cầu bảo hành", exception);
        }
    }

    private static JTextArea taoOThongTin() {
        JTextArea txa = new JTextArea(3, 18);
        txa.setFont(UIConfig.FONT_REGULAR);
        txa.setLineWrap(true);
        txa.setWrapStyleWord(true);
        return txa;
    }

    private static String dinhDangNgay(Timestamp ngay) {
        return ngay == null ? "" : ngay.toLocalDateTime().format(DINH_DANG_NGAY_GIO);
    }

    private void thongBao(String noiDung, int loai) {
        JOptionPane.showMessageDialog(this, noiDung, "Yêu cầu bảo hành", loai);
    }

    private void baoLoi(String tieuDe, SQLException exception) {
        JOptionPane.showMessageDialog(this, tieuDe + ":\n" + exception.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
