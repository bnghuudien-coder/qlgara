package gui;

import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.SwingUtilities;

public class FrmQuanLyTaiKhoan extends JPanel implements ActionListener {
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private final AppTextField txtMaNV = new AppTextField(16);
    private final AppTextField txtTenDangNhap = new AppTextField(16);
    private final AppTextField txtLocMaNV = new AppTextField(10);
    private final AppTextField txtLocTenDangNhap = new AppTextField(12);
    private final JPasswordField pwdMatKhau = new JPasswordField(16);
    private final JPasswordField pwdXacNhanMatKhau = new JPasswordField(16);
    private final AppComboBox<String> cboVaiTro = new AppComboBox<>();
    private final AppComboBox<String> cboTrangThai = new AppComboBox<>();
    private final JLabel lblThongBao = new AppLabel("Sẵn sàng", AppLabel.Style.MUTED);

    private final DefaultTableModel modelTaiKhoan = new DefaultTableModel(
            new Object[] {"Mã NV", "Họ tên", "Tên đăng nhập", "Vai trò", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final javax.swing.JTable tblTaiKhoan = FormUI.createTable(modelTaiKhoan);
    private final TableRowSorter<DefaultTableModel> sorterTaiKhoan = new TableRowSorter<>(modelTaiKhoan);

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

        tblTaiKhoan.setRowSorter(sorterTaiKhoan);
        tblTaiKhoan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTaiKhoan.setRowHeight(30);
        tblTaiKhoan.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblTaiKhoan.setFillsViewportHeight(true);
        int[] doRongCot = {110, 190, 170, 170, 130};
        for (int cot = 0; cot < doRongCot.length; cot++) {
            tblTaiKhoan.getColumnModel().getColumn(cot).setPreferredWidth(doRongCot[cot]);
        }
        tblTaiKhoan.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tblTaiKhoan.getSelectedRow() >= 0) {
                hienThiTaiKhoanDangChon();
            }
        });

        khoiTaoComboBox();
        taiDanhSachTaiKhoan();
        lamMoiForm();
    }

    private JPanel createSearchToolbar() {
        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);

        JPanel pnlLoc = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(createSearchField("Mã nhân viên", txtLocMaNV));
        pnlLoc.add(createSearchField("Tên đăng nhập", txtLocTenDangNhap));

        AppButton btnTaiLai = new AppButton("Tải lại", AppButton.Style.SECONDARY);
        btnTaiLai.addActionListener(this);

        pnlSearch.add(pnlLoc, BorderLayout.CENTER);
        pnlSearch.add(btnTaiLai, BorderLayout.EAST);

        themBoLangNghe(txtLocMaNV);
        themBoLangNghe(txtLocTenDangNhap);
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

    private void themBoLangNghe(AppTextField txtField) {
        txtField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                locDanhSachTaiKhoan();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                locDanhSachTaiKhoan();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                locDanhSachTaiKhoan();
            }
        });
    }

    private void khoiTaoComboBox() {
        cboVaiTro.setModel(new DefaultComboBoxModel<>(new String[] {
                "Chọn vai trò",
                "Quản lý",
                "Nhân viên",
                "Lễ tân",
                "Kỹ thuật viên",
                "Thu ngân"
        }));
        cboTrangThai.setModel(new DefaultComboBoxModel<>(new String[] {
                "Chọn trạng thái",
                "Hoạt động",
                "Không hoạt động",
                "Tạm khóa"
        }));
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

        AppButton btnThem = new AppButton("Thêm tài khoản");
        AppButton btnCapNhat = new AppButton("Cập nhật", AppButton.Style.SUCCESS);
        AppButton btnXoa = new AppButton("Xóa", AppButton.Style.DANGER);
        AppButton btnDatLaiMatKhau = new AppButton("Đặt lại mật khẩu", AppButton.Style.SECONDARY);
        AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);

        btnThem.addActionListener(this);
        btnCapNhat.addActionListener(this);
        btnXoa.addActionListener(this);
        btnDatLaiMatKhau.addActionListener(this);
        btnLamMoi.addActionListener(this);

        JPanel pnlButtons = FormUI.createButtonRow(btnThem, btnCapNhat, btnXoa, btnDatLaiMatKhau, btnLamMoi);
        pnlCard.add(FormUI.createSectionTitle("Thông tin tài khoản"), BorderLayout.NORTH);
        pnlCard.add(pnlFields, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }

    private JPanel createPasswordFieldPanel(JPasswordField passwordField) {
        JPanel pnlPassword = new JPanel(new BorderLayout());
        pnlPassword.setOpaque(false);
        pnlPassword.add(passwordField, BorderLayout.CENTER);
        return pnlPassword;
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

    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        switch (command) {
            case "Thêm tài khoản":
                themTaiKhoan();
                break;
            case "Cập nhật":
                capNhatTaiKhoan();
                break;
            case "Xóa":
                xoaTaiKhoan();
                break;
            case "Đặt lại mật khẩu":
                datLaiMatKhau();
                break;
            case "Làm mới":
                lamMoiForm();
                break;
            case "Tải lại":
                taiDanhSachTaiKhoan();
                lamMoiForm();
                break;
            default:
                break;
        }
    }

    private void taiDanhSachTaiKhoan() {
        try {
            modelTaiKhoan.setRowCount(0);
            java.util.List<TaiKhoan> danhSach = taiKhoanDAO.findAll();
            for (TaiKhoan taiKhoan : danhSach) {
                NhanVien nhanVien = nhanVienDAO.findById(taiKhoan.getMaNV());
                String hoTen = nhanVien != null ? nhanVien.getHoTen() : "";
                modelTaiKhoan.addRow(new Object[] {
                        taiKhoan.getMaNV(),
                        hoTen,
                        taiKhoan.getTenDangNhap(),
                        taiKhoan.getVaiTro(),
                        taiKhoan.getTrangThai()
                });
            }
            lblThongBao.setText("Đã tải " + modelTaiKhoan.getRowCount() + " tài khoản");
        } catch (SQLException exception) {
            lblThongBao.setText("Lỗi tải dữ liệu");
            JOptionPane.showMessageDialog(this,
                    "Không thể tải danh sách tài khoản: " + exception.getMessage(),
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void locDanhSachTaiKhoan() {
        String maNV = txtLocMaNV.getText().trim().toLowerCase();
        String tenDangNhap = txtLocTenDangNhap.getText().trim().toLowerCase();

        sorterTaiKhoan.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String rowMaNV = String.valueOf(entry.getValue(0)).trim().toLowerCase();
                String rowTenDangNhap = String.valueOf(entry.getValue(2)).trim().toLowerCase();

                boolean matchMaNV = maNV.isEmpty() || rowMaNV.contains(maNV);
                boolean matchTenDangNhap = tenDangNhap.isEmpty() || rowTenDangNhap.contains(tenDangNhap);
                return matchMaNV && matchTenDangNhap;
            }
        });
    }

    private void hienThiTaiKhoanDangChon() {
        int rowView = tblTaiKhoan.getSelectedRow();
        if (rowView < 0) {
            return;
        }

        int rowModel = tblTaiKhoan.convertRowIndexToModel(rowView);
        String maNV = String.valueOf(modelTaiKhoan.getValueAt(rowModel, 0));

        try {
            TaiKhoan taiKhoan = taiKhoanDAO.findById(maNV);
            if (taiKhoan == null) {
                return;
            }

            txtMaNV.setEditable(false);
            txtMaNV.setText(taiKhoan.getMaNV());
            txtTenDangNhap.setText(taiKhoan.getTenDangNhap());
            pwdMatKhau.setText("");
            pwdXacNhanMatKhau.setText("");
            cboVaiTro.setSelectedItem(taiKhoan.getVaiTro() != null ? taiKhoan.getVaiTro() : "Chọn vai trò");
            cboTrangThai.setSelectedItem(taiKhoan.getTrangThai() != null ? taiKhoan.getTrangThai() : "Chọn trạng thái");
            lblThongBao.setText("Đang chỉnh sửa tài khoản: " + taiKhoan.getTenDangNhap());
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Không thể lấy thông tin tài khoản: " + exception.getMessage(),
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void lamMoiForm() {
        txtMaNV.setEditable(true);
        txtMaNV.setText("");
        txtTenDangNhap.setText("");
        pwdMatKhau.setText("");
        pwdXacNhanMatKhau.setText("");
        cboVaiTro.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        tblTaiKhoan.clearSelection();
        lblThongBao.setText("Sẵn sàng");
    }

    private boolean validateForm(boolean batBuocMatKhau) {
        String maNV = txtMaNV.getText().trim();
        String tenDangNhap = txtTenDangNhap.getText().trim();
        String matKhau = new String(pwdMatKhau.getPassword()).trim();
        String xacNhanMatKhau = new String(pwdXacNhanMatKhau.getPassword()).trim();

        if (maNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã nhân viên.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (tenDangNhap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đăng nhập.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (cboVaiTro.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn vai trò cho tài khoản.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (cboTrangThai.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn trạng thái tài khoản.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (batBuocMatKhau && matKhau.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!matKhau.isEmpty() && !matKhau.equals(xacNhanMatKhau)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp.", "Sai thông tin", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!matKhau.isEmpty() && matKhau.length() < 6) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất 6 ký tự.", "Sai thông tin", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void themTaiKhoan() {
        if (!validateForm(true)) {
            return;
        }

        String maNV = txtMaNV.getText().trim();
        String tenDangNhap = txtTenDangNhap.getText().trim();
        String matKhau = new String(pwdMatKhau.getPassword()).trim();

        try {
            if (taiKhoanDAO.findById(maNV) != null) {
                JOptionPane.showMessageDialog(this,
                        "Mã nhân viên này đã có tài khoản. Vui lòng chọn nhân viên khác hoặc cập nhật tài khoản hiện có.",
                        "Tài khoản đã tồn tại",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setMaNV(maNV);
            taiKhoan.setTenDangNhap(tenDangNhap);
            taiKhoan.setMatKhau(matKhau);
            taiKhoan.setVaiTro(String.valueOf(cboVaiTro.getSelectedItem()));
            taiKhoan.setTrangThai(String.valueOf(cboTrangThai.getSelectedItem()));

            boolean thanhCong = taiKhoanDAO.insert(taiKhoan);
            if (thanhCong) {
                lblThongBao.setText("Đã tạo tài khoản cho nhân viên " + maNV);
                JOptionPane.showMessageDialog(this, "Tạo tài khoản thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                taiDanhSachTaiKhoan();
                lamMoiForm();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể tạo tài khoản.", "Thất bại", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException exception) {
            lblThongBao.setText("Lỗi tạo tài khoản");
            JOptionPane.showMessageDialog(this,
                    "Không thể tạo tài khoản: " + exception.getMessage(),
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void capNhatTaiKhoan() {
        String maNV = txtMaNV.getText().trim();
        if (maNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần cập nhật.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validateForm(false)) {
            return;
        }

        try {
            TaiKhoan taiKhoan = taiKhoanDAO.findById(maNV);
            if (taiKhoan == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy tài khoản để cập nhật.", "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
                return;
            }

            taiKhoan.setTenDangNhap(txtTenDangNhap.getText().trim());
            taiKhoan.setVaiTro(String.valueOf(cboVaiTro.getSelectedItem()));
            taiKhoan.setTrangThai(String.valueOf(cboTrangThai.getSelectedItem()));

            String matKhau = new String(pwdMatKhau.getPassword()).trim();
            if (!matKhau.isEmpty()) {
                taiKhoan.setMatKhau(matKhau);
            }

            boolean thanhCong = taiKhoanDAO.update(taiKhoan);
            if (thanhCong) {
                lblThongBao.setText("Đã cập nhật tài khoản: " + taiKhoan.getTenDangNhap());
                JOptionPane.showMessageDialog(this, "Cập nhật tài khoản thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                taiDanhSachTaiKhoan();
                lamMoiForm();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật tài khoản.", "Thất bại", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException exception) {
            lblThongBao.setText("Lỗi cập nhật tài khoản");
            JOptionPane.showMessageDialog(this,
                    "Không thể cập nhật tài khoản: " + exception.getMessage(),
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaTaiKhoan() {
        String maNV = txtMaNV.getText().trim();
        if (maNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần xóa.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa tài khoản của nhân viên " + maNV + " không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean thanhCong = taiKhoanDAO.deleteById(maNV);
            if (thanhCong) {
                lblThongBao.setText("Đã xóa tài khoản: " + maNV);
                JOptionPane.showMessageDialog(this, "Xóa tài khoản thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                taiDanhSachTaiKhoan();
                lamMoiForm();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản.", "Thất bại", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException exception) {
            lblThongBao.setText("Lỗi xóa tài khoản");
            JOptionPane.showMessageDialog(this,
                    "Không thể xóa tài khoản: " + exception.getMessage(),
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void datLaiMatKhau() {
        String maNV = txtMaNV.getText().trim();
        if (maNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần đặt lại mật khẩu.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Bạn có muốn đặt lại mật khẩu cho tài khoản " + maNV + " về mặc định không?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            TaiKhoan taiKhoan = taiKhoanDAO.findById(maNV);
            if (taiKhoan == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy tài khoản cần đặt lại mật khẩu.", "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
                return;
            }
            taiKhoan.setMatKhau("123456");
            boolean thanhCong = taiKhoanDAO.update(taiKhoan);
            if (thanhCong) {
                lblThongBao.setText("Đã đặt lại mật khẩu cho: " + taiKhoan.getTenDangNhap());
                JOptionPane.showMessageDialog(this,
                        "Đặt lại mật khẩu thành công. Mật khẩu mặc định mới: 123456",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                pwdMatKhau.setText("");
                pwdXacNhanMatKhau.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Không thể đặt lại mật khẩu.", "Thất bại", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Không thể đặt lại mật khẩu: " + exception.getMessage(),
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
