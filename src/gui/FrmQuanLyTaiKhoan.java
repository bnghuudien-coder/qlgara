package gui;

import controller.TaiKhoanController;
import entity.NhanVien;
import entity.TaiKhoan;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class FrmQuanLyTaiKhoan extends JPanel {
    private final AppTextField txtMaNV = new AppTextField(16);
    private final AppTextField txtTenDangNhap = new AppTextField(16);
    private final AppTextField txtLocMaNV = new AppTextField(10);
    private final AppTextField txtLocTenDangNhap = new AppTextField(12);
    private final JPasswordField pwdMatKhau = new JPasswordField(16);
    private final JPasswordField pwdXacNhanMatKhau = new JPasswordField(16);
    private final char kyTuCheMatKhau = pwdMatKhau.getEchoChar();
    private final JCheckBox chkHienMatKhau = new JCheckBox("Hiện mật khẩu");
    private final AppComboBox<String> cboVaiTro = new AppComboBox<>();
    private final AppComboBox<String> cboTrangThai = new AppComboBox<>();
    private final AppLabel lblThongBao = new AppLabel("Sẵn sàng", AppLabel.Style.MUTED);

    private final AppButton btnThem = new AppButton("Thêm tài khoản");
    private final AppButton btnCapNhat = new AppButton("Cập nhật", AppButton.Style.SUCCESS);
    private final AppButton btnXoa = new AppButton("Xóa", AppButton.Style.DANGER);
    private final AppButton btnDatLaiMatKhau = new AppButton("Đặt lại mật khẩu", AppButton.Style.SECONDARY);
    private final AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
    private final AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);

    private final DefaultTableModel modelTaiKhoan = new DefaultTableModel(
            new Object[] {"Mã NV", "Họ tên", "Tên đăng nhập", "Vai trò", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final javax.swing.JTable tblTaiKhoan = FormUI.createTable(modelTaiKhoan);
    private final TableRowSorter<DefaultTableModel> sorterTaiKhoan = new TableRowSorter<>(modelTaiKhoan);
    private final TaiKhoanController controller;

    public FrmQuanLyTaiKhoan() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 8));
        pnlContent.setOpaque(false);

        JSplitPane splContent = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                createFormCard(),
                FormUI.createTableCard("Danh sách tài khoản", createSearchToolbar(), new AppScrollPane(tblTaiKhoan))
        );
        splContent.setBorder(null);
        splContent.setResizeWeight(0.42);
        splContent.setDividerSize(7);
        splContent.setEnabled(false);
        SwingUtilities.invokeLater(() -> splContent.setDividerLocation(0.42));

        pnlContent.add(splContent, BorderLayout.CENTER);
        add(FormUI.createPage(
                "QUẢN LÝ TÀI KHOẢN",
                "Quản lý người dùng, vai trò và trạng thái tài khoản trong hệ thống gara",
                pnlContent), BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);

        khoiTaoGiaoDien();
        controller = new TaiKhoanController(this);
        controller.khoiTao();
    }

    private JPanel createSearchToolbar() {
        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);

        JPanel pnlLoc = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(createSearchField("Mã nhân viên", txtLocMaNV));
        pnlLoc.add(createSearchField("Tên đăng nhập", txtLocTenDangNhap));

        pnlSearch.add(pnlLoc, BorderLayout.CENTER);
        pnlSearch.add(btnTaiLai, BorderLayout.EAST);
        return pnlSearch;
    }

    private JPanel createSearchField(String labelText, AppTextField txtField) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 3));
        pnlField.setOpaque(false);
        JLabel lblField = new AppLabel(labelText, AppLabel.Style.MUTED);
        pnlField.add(lblField, BorderLayout.NORTH);
        pnlField.add(txtField, BorderLayout.CENTER);
        return pnlField;
    }

    private JPanel createStatusPanel() {
        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        pnlStatus.add(lblThongBao, BorderLayout.WEST);
        return pnlStatus;
    }

    private void khoiTaoGiaoDien() {
        cboVaiTro.setModel(new DefaultComboBoxModel<>(new String[] {
                "Chọn vai trò", "Quản lý", "Nhân viên", "Lễ tân", "Kỹ thuật viên", "Thu ngân"
        }));
        cboTrangThai.setModel(new DefaultComboBoxModel<>(new String[] {
                "Chọn trạng thái", "Hoạt động", "Không hoạt động", "Tạm khóa"
        }));
        tblTaiKhoan.setRowSorter(sorterTaiKhoan);
        tblTaiKhoan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTaiKhoan.setRowHeight(30);
        tblTaiKhoan.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblTaiKhoan.setFillsViewportHeight(true);
        int[] doRongCot = {110, 190, 170, 170, 130};
        for (int cot = 0; cot < doRongCot.length; cot++) {
            tblTaiKhoan.getColumnModel().getColumn(cot).setPreferredWidth(doRongCot[cot]);
        }
    }

    private JPanel createFormCard() {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.setLayout(new BorderLayout(0, 6));

        JPanel pnlFields = FormUI.createFieldsPanel();
        FormUI.addField(pnlFields, "Mã nhân viên *", txtMaNV, 0, 0);
        FormUI.addField(pnlFields, "Tên đăng nhập *", txtTenDangNhap, 2, 0);
        FormUI.addField(pnlFields, "Mật khẩu *", new AppPasswordField(pwdMatKhau), 0, 1);
        FormUI.addField(pnlFields, "Xác nhận mật khẩu *", new AppPasswordField(pwdXacNhanMatKhau), 2, 1);
        FormUI.addField(pnlFields, "Vai trò", cboVaiTro, 0, 2);
        FormUI.addField(pnlFields, "Trạng thái", cboTrangThai, 2, 2);
        FormUI.addField(pnlFields, "Bảo mật", chkHienMatKhau, 0, 3);

        JPanel pnlButtons = FormUI.createButtonRow(
                btnThem, btnCapNhat, btnXoa, btnDatLaiMatKhau, btnLamMoi);
        pnlCard.add(FormUI.createSectionTitle("Thông tin tài khoản"), BorderLayout.NORTH);
        pnlCard.add(pnlFields, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }

    private static final class AppPasswordField extends JPanel {
        private AppPasswordField(JPasswordField passwordField) {
            setLayout(new BorderLayout());
            setOpaque(false);
            passwordField.setFont(UIConfig.FONT_REGULAR);
            passwordField.setForeground(UIConfig.COLOR_TEXT);
            passwordField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(UIConfig.COLOR_BORDER, 1, true),
                    new javax.swing.border.EmptyBorder(6, 9, 6, 9)));
            add(passwordField, BorderLayout.CENTER);
        }
    }

    public void hienThiDanhSachTaiKhoan(List<TaiKhoan> danhSach) {
        modelTaiKhoan.setRowCount(0);
        for (TaiKhoan taiKhoan : danhSach) {
            NhanVien nhanVien = taiKhoan.getNhanVien();
            modelTaiKhoan.addRow(new Object[] {
                    nhanVien == null ? "" : nhanVien.getMaNV(),
                    nhanVien == null ? "" : nhanVien.getHoTen(),
                    taiKhoan.getTenDangNhap(),
                    taiKhoan.getVaiTro(),
                    taiKhoan.getTrangThai()
            });
        }
        thongBao("Đã tải " + modelTaiKhoan.getRowCount() + " tài khoản");
    }

    public void locDanhSachTaiKhoan() {
        String maNV = txtLocMaNV.getText().trim().toLowerCase();
        String tenDangNhap = txtLocTenDangNhap.getText().trim().toLowerCase();
        sorterTaiKhoan.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String rowMaNV = String.valueOf(entry.getValue(0)).trim().toLowerCase();
                String rowTenDangNhap = String.valueOf(entry.getValue(2)).trim().toLowerCase();
                return (maNV.isEmpty() || rowMaNV.contains(maNV))
                        && (tenDangNhap.isEmpty() || rowTenDangNhap.contains(tenDangNhap));
            }
        });
    }

    public String getMaNVDangChon() {
        int rowView = tblTaiKhoan.getSelectedRow();
        if (rowView < 0) {
            return null;
        }
        int rowModel = tblTaiKhoan.convertRowIndexToModel(rowView);
        return String.valueOf(modelTaiKhoan.getValueAt(rowModel, 0));
    }

    public void hienThiTaiKhoan(TaiKhoan taiKhoan) {
        txtMaNV.setEditable(false);
        txtMaNV.setText(taiKhoan.getNhanVien().getMaNV());
        txtTenDangNhap.setText(taiKhoan.getTenDangNhap());
        pwdMatKhau.setText(taiKhoan.getMatKhau());
        pwdXacNhanMatKhau.setText(taiKhoan.getMatKhau());
        capNhatHienThiMatKhau();
        cboVaiTro.setSelectedItem(taiKhoan.getVaiTro() != null ? taiKhoan.getVaiTro() : "Chọn vai trò");
        cboTrangThai.setSelectedItem(taiKhoan.getTrangThai() != null ? taiKhoan.getTrangThai() : "Chọn trạng thái");
        thongBao("Đang chỉnh sửa tài khoản: " + taiKhoan.getTenDangNhap());
    }

    public void lamMoiForm() {
        txtMaNV.setEditable(true);
        txtMaNV.setText("");
        txtTenDangNhap.setText("");
        pwdMatKhau.setText("");
        pwdXacNhanMatKhau.setText("");
        chkHienMatKhau.setSelected(false);
        capNhatHienThiMatKhau();
        cboVaiTro.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        tblTaiKhoan.clearSelection();
        thongBao("Sẵn sàng");
    }

    public void capNhatHienThiMatKhau() {
        char kyTuHienThi = chkHienMatKhau.isSelected() ? (char) 0 : kyTuCheMatKhau;
        pwdMatKhau.setEchoChar(kyTuHienThi);
        pwdXacNhanMatKhau.setEchoChar(kyTuHienThi);
    }

    public boolean validateForm(boolean batBuocMatKhau) {
        String maNV = getMaNVNhap();
        String tenDangNhap = getTenDangNhapNhap();
        String matKhau = getMatKhauNhap();
        String xacNhanMatKhau = new String(pwdXacNhanMatKhau.getPassword()).trim();

        if (maNV.isEmpty()) {
            hienThiCanhBao("Thiếu thông tin", "Vui lòng nhập mã nhân viên.");
            return false;
        }
        if (tenDangNhap.isEmpty()) {
            hienThiCanhBao("Thiếu thông tin", "Vui lòng nhập tên đăng nhập.");
            return false;
        }
        if (cboVaiTro.getSelectedIndex() <= 0) {
            hienThiCanhBao("Thiếu thông tin", "Vui lòng chọn vai trò cho tài khoản.");
            return false;
        }
        if (cboTrangThai.getSelectedIndex() <= 0) {
            hienThiCanhBao("Thiếu thông tin", "Vui lòng chọn trạng thái tài khoản.");
            return false;
        }
        if (batBuocMatKhau && matKhau.isEmpty()) {
            hienThiCanhBao("Thiếu thông tin", "Vui lòng nhập mật khẩu.");
            return false;
        }
        if (!matKhau.isEmpty() && !matKhau.equals(xacNhanMatKhau)) {
            hienThiCanhBao("Sai thông tin", "Mật khẩu xác nhận không khớp.");
            return false;
        }
        if (!matKhau.isEmpty() && matKhau.length() < 6) {
            hienThiCanhBao("Sai thông tin", "Mật khẩu phải có ít nhất 6 ký tự.");
            return false;
        }
        return true;
    }

    public String getMaNVNhap() { return txtMaNV.getText().trim(); }
    public String getTenDangNhapNhap() { return txtTenDangNhap.getText().trim(); }
    public String getMatKhauNhap() { return new String(pwdMatKhau.getPassword()).trim(); }
    public String getVaiTroNhap() { return String.valueOf(cboVaiTro.getSelectedItem()); }
    public String getTrangThaiNhap() { return String.valueOf(cboTrangThai.getSelectedItem()); }

    public void xoaMatKhauNhap() {
        pwdMatKhau.setText("");
        pwdXacNhanMatKhau.setText("");
    }

    public boolean xacNhanXoa(String maNV) {
        return JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa tài khoản của nhân viên " + maNV + " không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public boolean xacNhanDatLaiMatKhau(String maNV) {
        return JOptionPane.showConfirmDialog(
                this,
                "Bạn có muốn đặt lại mật khẩu cho tài khoản " + maNV + " về mặc định không?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void thongBao(String message) {
        lblThongBao.setForeground(UIConfig.COLOR_MUTED);
        lblThongBao.setText(message);
    }

    public void hienThiLoi(String tieuDe, String message) {
        lblThongBao.setForeground(UIConfig.COLOR_DANGER);
        lblThongBao.setText("Lỗi dữ liệu");
        JOptionPane.showMessageDialog(this, message, tieuDe, JOptionPane.ERROR_MESSAGE);
    }

    public void hienThiCanhBao(String tieuDe, String message) {
        JOptionPane.showMessageDialog(this, message, tieuDe, JOptionPane.WARNING_MESSAGE);
    }

    public void hienThiThanhCong(String message) {
        JOptionPane.showMessageDialog(this, message, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    public void hienThiThatBai(String message) {
        JOptionPane.showMessageDialog(this, message, "Thất bại", JOptionPane.ERROR_MESSAGE);
    }

    public AppButton getBtnThem() { return btnThem; }
    public AppButton getBtnCapNhat() { return btnCapNhat; }
    public AppButton getBtnXoa() { return btnXoa; }
    public AppButton getBtnDatLaiMatKhau() { return btnDatLaiMatKhau; }
    public AppButton getBtnLamMoi() { return btnLamMoi; }
    public AppButton getBtnTaiLai() { return btnTaiLai; }
    public JCheckBox getChkHienMatKhau() { return chkHienMatKhau; }
    public AppTextField getTxtLocMaNV() { return txtLocMaNV; }
    public AppTextField getTxtLocTenDangNhap() { return txtLocTenDangNhap; }
    public javax.swing.JTable getTblTaiKhoan() { return tblTaiKhoan; }
}
