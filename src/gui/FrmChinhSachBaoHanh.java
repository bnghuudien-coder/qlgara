package gui;

import dao.ChinhSachBaoHanhDAO;
import entity.ChinhSachBaoHanh;
import entity.PhuTung;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class FrmChinhSachBaoHanh extends JPanel {
    private record DonViThoiGian(String ma, String ten) {
        @Override
        public String toString() {
            return ten;
        }
    }

    private final ChinhSachBaoHanhDAO chinhSachBaoHanhDAO = new ChinhSachBaoHanhDAO();
    private final AppTextField txtMaCSBH = new AppTextField(12);
    private final AppTextField txtTenChinhSach = new AppTextField(24);
    private final javax.swing.JSpinner spnThoiHan = new javax.swing.JSpinner(
            new javax.swing.SpinnerNumberModel(12, 1, 9999, 1));
    private final JComboBox<DonViThoiGian> cboDonViThoiGian = new JComboBox<>(new DonViThoiGian[] {
            new DonViThoiGian("THANG", "Tháng"),
            new DonViThoiGian("NGAY", "Ngày"),
            new DonViThoiGian("NAM", "Năm")
    });
    private final JCheckBox chkTrangThai = new JCheckBox("Đang áp dụng", true);
    private final JTextArea txaDieuKien = taoOThongTin();
    private final JTextArea txaTruongHopTuChoi = taoOThongTin();
    private final JTextArea txaNoiDungBaoHanh = taoOThongTin();
    private final AppTextField txtTimPhuTung = new AppTextField(16);
    private final AppTextField txtTimChinhSach = new AppTextField(18);
    private final Set<String> dsMaPhuTungDangChon = new LinkedHashSet<>();
    private final List<PhuTung> dsPhuTung = new ArrayList<>();
    private boolean dangNapBangPhuTung;
    private boolean dangThemMoi = true;

    private final DefaultTableModel modelPhuTung = new DefaultTableModel(
            new Object[] { "Áp dụng", "Mã phụ tùng", "Tên phụ tùng", "Tồn kho" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 0 ? Boolean.class : column == 3 ? Integer.class : String.class;
        }
    };
    private final JTable tblPhuTung = FormUI.createTable(modelPhuTung);
    private final DefaultTableModel modelChinhSach = new DefaultTableModel(
            new Object[] { "Mã chính sách", "Tên chính sách", "Thời hạn", "Phụ tùng", "Trạng thái", "Ngày tạo" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblChinhSach = FormUI.createTable(modelChinhSach);

    public FrmChinhSachBaoHanh() {
        setLayout(new BorderLayout());
        setOpaque(false);
        txtMaCSBH.setEditable(false);
        tblPhuTung.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChinhSach.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChinhSach.setAutoCreateRowSorter(false);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoKhungChinhSach(), BorderLayout.NORTH);
        pnlNoiDung.add(taoBangChinhSach(), BorderLayout.CENTER);
        add(FormUI.createPage("CHÍNH SÁCH BẢO HÀNH",
                "Quản lý thời hạn, điều kiện và phụ tùng được áp dụng bảo hành", pnlNoiDung),
                BorderLayout.CENTER);

        tblPhuTung.getModel().addTableModelListener(event -> {
            if (!dangNapBangPhuTung && event.getType() == javax.swing.event.TableModelEvent.UPDATE
                    && event.getColumn() == 0 && event.getFirstRow() >= 0) {
                int row = event.getFirstRow();
                String maPhuTung = String.valueOf(modelPhuTung.getValueAt(row, 1));
                if (Boolean.TRUE.equals(modelPhuTung.getValueAt(row, 0))) {
                    dsMaPhuTungDangChon.add(maPhuTung);
                } else {
                    dsMaPhuTungDangChon.remove(maPhuTung);
                }
            }
        });
        tblChinhSach.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tblChinhSach.getSelectedRow() >= 0) {
                hienThiChinhSachDangChon();
            }
        });
        txtTimPhuTung.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) { hienThiDanhSachPhuTung(); }
            @Override
            public void removeUpdate(DocumentEvent event) { hienThiDanhSachPhuTung(); }
            @Override
            public void changedUpdate(DocumentEvent event) { hienThiDanhSachPhuTung(); }
        });
        txtTimChinhSach.addActionListener(event -> taiDanhSachChinhSach());

        taiDuLieuBanDau();
    }

    private JPanel taoKhungChinhSach() {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.add(FormUI.createSectionTitle("Thông tin chính sách"), BorderLayout.NORTH);

        JPanel pnlNoiDung = new JPanel(new BorderLayout(12, 0));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(taoKhungThongTin(), BorderLayout.CENTER);
        pnlNoiDung.add(taoKhungPhuTung(), BorderLayout.EAST);
        pnlCard.add(pnlNoiDung, BorderLayout.CENTER);
        return pnlCard;
    }

    private JPanel taoKhungThongTin() {
        JPanel pnlThongTin = new JPanel(new BorderLayout(0, 8));
        pnlThongTin.setOpaque(false);

        JPanel pnlTruong = FormUI.createFieldsPanel();
        FormUI.addField(pnlTruong, "Mã chính sách", txtMaCSBH, 0, 0);
        FormUI.addField(pnlTruong, "Tên chính sách *", txtTenChinhSach, 2, 0);
        JPanel pnlThoiHan = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlThoiHan.setOpaque(false);
        pnlThoiHan.add(spnThoiHan);
        pnlThoiHan.add(cboDonViThoiGian);
        FormUI.addField(pnlTruong, "Thời hạn *", pnlThoiHan, 0, 1);
        FormUI.addField(pnlTruong, "Trạng thái", chkTrangThai, 2, 1);
        pnlThongTin.add(pnlTruong, BorderLayout.NORTH);

        JPanel pnlMoTa = new JPanel(new GridLayout(1, 3, 8, 0));
        pnlMoTa.setOpaque(false);
        pnlMoTa.add(taoTruongMoTa("Nội dung bảo hành", txaNoiDungBaoHanh));
        pnlMoTa.add(taoTruongMoTa("Điều kiện bảo hành", txaDieuKien));
        pnlMoTa.add(taoTruongMoTa("Trường hợp từ chối", txaTruongHopTuChoi));
        pnlThongTin.add(pnlMoTa, BorderLayout.CENTER);

        AppButton btnThem = new AppButton("Thêm");
        AppButton btnCapNhat = new AppButton("Cập nhật", AppButton.Style.SUCCESS);
        AppButton btnXoa = new AppButton("Xóa", AppButton.Style.DANGER);
        AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
        btnThem.addActionListener(event -> luuChinhSach(true));
        btnCapNhat.addActionListener(event -> luuChinhSach(false));
        btnXoa.addActionListener(event -> xoaChinhSach());
        btnLamMoi.addActionListener(event -> lamMoiForm());
        JPanel pnlNut = FormUI.createButtonRow(btnThem, btnCapNhat, btnXoa, btnLamMoi);
        pnlThongTin.add(pnlNut, BorderLayout.SOUTH);
        return pnlThongTin;
    }

    private JPanel taoTruongMoTa(String nhan, JTextArea txaNoiDung) {
        JPanel pnlTruong = new JPanel(new BorderLayout(0, 5));
        pnlTruong.setOpaque(false);
        pnlTruong.add(new AppLabel(nhan), BorderLayout.NORTH);
        pnlTruong.add(new AppScrollPane(txaNoiDung), BorderLayout.CENTER);
        return pnlTruong;
    }

    private JPanel taoKhungPhuTung() {
        JPanel pnlPhuTung = new JPanel(new BorderLayout(0, 8));
        pnlPhuTung.setOpaque(false);
        pnlPhuTung.setPreferredSize(new Dimension(410, 250));
        pnlPhuTung.add(FormUI.createSectionTitle("Phụ tùng áp dụng *"), BorderLayout.NORTH);

        JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 6));
        pnlDanhSach.setOpaque(false);
        txtTimPhuTung.setToolTipText("Lọc phụ tùng theo mã hoặc tên");
        pnlDanhSach.add(txtTimPhuTung, BorderLayout.NORTH);
        pnlDanhSach.add(new AppScrollPane(tblPhuTung), BorderLayout.CENTER);
        pnlPhuTung.add(pnlDanhSach, BorderLayout.CENTER);
        return pnlPhuTung;
    }

    private JPanel taoBangChinhSach() {
        JPanel pnlBang = FormUI.createCard();
        JPanel pnlThanhTimKiem = new JPanel(new BorderLayout(10, 0));
        pnlThanhTimKiem.setOpaque(false);
        pnlThanhTimKiem.add(FormUI.createSectionTitle("Danh sách chính sách"), BorderLayout.WEST);
        txtTimChinhSach.setToolTipText("Tìm theo mã, tên, nội dung hoặc điều kiện bảo hành");
        AppButton btnTimKiem = new AppButton("Tìm kiếm");
        btnTimKiem.addActionListener(event -> taiDanhSachChinhSach());
        JPanel pnlTimKiem = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlTimKiem.setOpaque(false);
        pnlTimKiem.add(txtTimChinhSach);
        pnlTimKiem.add(btnTimKiem);
        pnlThanhTimKiem.add(pnlTimKiem, BorderLayout.EAST);
        pnlBang.add(pnlThanhTimKiem, BorderLayout.NORTH);
        pnlBang.add(new AppScrollPane(tblChinhSach), BorderLayout.CENTER);
        return pnlBang;
    }

    private void taiDuLieuBanDau() {
        try {
            dsPhuTung.clear();
            dsPhuTung.addAll(chinhSachBaoHanhDAO.layDanhSachPhuTung());
            txtMaCSBH.setText(chinhSachBaoHanhDAO.layMaTiepTheo());
            hienThiDanhSachPhuTung();
            taiDanhSachChinhSach();
        } catch (SQLException exception) {
            baoLoi("Không thể tải dữ liệu chính sách bảo hành", exception);
            txtMaCSBH.setText("");
        }
    }

    private void hienThiDanhSachPhuTung() {
        String tuKhoa = txtTimPhuTung.getText().trim().toLowerCase(java.util.Locale.ROOT);
        dangNapBangPhuTung = true;
        modelPhuTung.setRowCount(0);
        for (PhuTung phuTung : dsPhuTung) {
            String ma = phuTung.getMaPhuTung();
            String ten = phuTung.getTenPhuTung();
            if (!tuKhoa.isEmpty()
                    && !ma.toLowerCase(java.util.Locale.ROOT).contains(tuKhoa)
                    && !ten.toLowerCase(java.util.Locale.ROOT).contains(tuKhoa)) {
                continue;
            }
            modelPhuTung.addRow(new Object[] {
                    dsMaPhuTungDangChon.contains(ma), ma, ten, phuTung.getSoLuongTon()
            });
        }
        dangNapBangPhuTung = false;
    }

    private void taiDanhSachChinhSach() {
        try {
            modelChinhSach.setRowCount(0);
            for (ChinhSachBaoHanh chinhSach : chinhSachBaoHanhDAO.timKiem(txtTimChinhSach.getText())) {
                modelChinhSach.addRow(new Object[] {
                        chinhSach.getMaCSBH(),
                        chinhSach.getTenChinhSach(),
                        hienThiThoiHan(chinhSach.getThoiHanBaoHanh(), chinhSach.getDonViThoiGian()),
                        chinhSach.getDsMaPhuTung().size(),
                        chinhSach.isTrangThai() ? "Đang áp dụng" : "Ngưng áp dụng",
                        chinhSach.getNgayTao() == null ? "" : chinhSach.getNgayTao().toLocalDateTime()
                                .toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                });
            }
        } catch (SQLException exception) {
            baoLoi("Không thể tải danh sách chính sách bảo hành", exception);
        }
    }

    private void hienThiChinhSachDangChon() {
        String maCSBH = String.valueOf(modelChinhSach.getValueAt(tblChinhSach.getSelectedRow(), 0));
        try {
            for (ChinhSachBaoHanh chinhSach : chinhSachBaoHanhDAO.timKiem(maCSBH)) {
                if (!maCSBH.equals(chinhSach.getMaCSBH())) {
                    continue;
                }
                dangThemMoi = false;
                txtMaCSBH.setText(chinhSach.getMaCSBH());
                txtTenChinhSach.setText(chinhSach.getTenChinhSach());
                spnThoiHan.setValue(chinhSach.getThoiHanBaoHanh());
                chonDonViThoiGian(chinhSach.getDonViThoiGian());
                txaDieuKien.setText(chinhSach.getDieuKienBaoHanh());
                txaTruongHopTuChoi.setText(chinhSach.getTruongHopTuChoi());
                txaNoiDungBaoHanh.setText(chinhSach.getNoiDungBaoHanh());
                chkTrangThai.setSelected(chinhSach.isTrangThai());
                dsMaPhuTungDangChon.clear();
                dsMaPhuTungDangChon.addAll(chinhSach.getDsMaPhuTung());
                hienThiDanhSachPhuTung();
                return;
            }
        } catch (SQLException exception) {
            baoLoi("Không thể tải thông tin chính sách", exception);
        }
    }

    private void luuChinhSach(boolean themMoi) {
        String maCSBH = txtMaCSBH.getText().trim();
        if (!themMoi && maCSBH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chính sách cần cập nhật.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (txtTenChinhSach.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Tên chính sách không được để trống.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            txtTenChinhSach.requestFocusInWindow();
            return;
        }
        if (dsMaPhuTungDangChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một phụ tùng áp dụng.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DonViThoiGian donVi = (DonViThoiGian) cboDonViThoiGian.getSelectedItem();
        ChinhSachBaoHanh chinhSach = new ChinhSachBaoHanh();
        chinhSach.setMaCSBH(maCSBH);
        chinhSach.setTenChinhSach(txtTenChinhSach.getText().trim());
        chinhSach.setThoiHanBaoHanh((Integer) spnThoiHan.getValue());
        chinhSach.setDonViThoiGian(donVi.ma());
        chinhSach.setDieuKienBaoHanh(txaDieuKien.getText().trim());
        chinhSach.setTruongHopTuChoi(txaTruongHopTuChoi.getText().trim());
        chinhSach.setNoiDungBaoHanh(txaNoiDungBaoHanh.getText().trim());
        chinhSach.setTrangThai(chkTrangThai.isSelected());
        chinhSach.setDsMaPhuTung(new ArrayList<>(dsMaPhuTungDangChon));

        try {
            chinhSachBaoHanhDAO.luu(chinhSach, themMoi);
            dangThemMoi = false;
            txtTimChinhSach.setText("");
            taiDanhSachChinhSach();
            chonChinhSach(chinhSach.getMaCSBH());
            JOptionPane.showMessageDialog(this,
                    (themMoi ? "Đã thêm" : "Đã cập nhật") + " chính sách " + chinhSach.getMaCSBH() + ".",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException exception) {
            baoLoi(themMoi ? "Không thể thêm chính sách bảo hành" : "Không thể cập nhật chính sách bảo hành",
                    exception);
        }
    }

    private void xoaChinhSach() {
        String maCSBH = txtMaCSBH.getText().trim();
        if (dangThemMoi || maCSBH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chính sách cần xóa.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int luaChon = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa chính sách " + maCSBH + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (!chinhSachBaoHanhDAO.xoa(maCSBH)) {
                throw new SQLException("Không tìm thấy chính sách cần xóa: " + maCSBH);
            }
            lamMoiForm();
            taiDanhSachChinhSach();
        } catch (SQLException exception) {
            baoLoi("Không thể xóa chính sách bảo hành", exception);
        }
    }

    private void lamMoiForm() {
        tblChinhSach.clearSelection();
        dangThemMoi = true;
        try {
            txtMaCSBH.setText(chinhSachBaoHanhDAO.layMaTiepTheo());
        } catch (SQLException exception) {
            baoLoi("Không thể tạo mã chính sách bảo hành", exception);
            txtMaCSBH.setText("");
        }
        txtTenChinhSach.setText("");
        spnThoiHan.setValue(12);
        cboDonViThoiGian.setSelectedIndex(0);
        txaDieuKien.setText("");
        txaTruongHopTuChoi.setText("");
        txaNoiDungBaoHanh.setText("");
        chkTrangThai.setSelected(true);
        dsMaPhuTungDangChon.clear();
        hienThiDanhSachPhuTung();
    }

    private void chonChinhSach(String maCSBH) {
        for (int row = 0; row < modelChinhSach.getRowCount(); row++) {
            if (maCSBH.equals(modelChinhSach.getValueAt(row, 0))) {
                tblChinhSach.setRowSelectionInterval(row, row);
                tblChinhSach.scrollRectToVisible(tblChinhSach.getCellRect(row, 0, true));
                return;
            }
        }
    }

    private void chonDonViThoiGian(String maDonVi) {
        for (int index = 0; index < cboDonViThoiGian.getItemCount(); index++) {
            if (cboDonViThoiGian.getItemAt(index).ma().equals(maDonVi)) {
                cboDonViThoiGian.setSelectedIndex(index);
                return;
            }
        }
    }

    private static String hienThiThoiHan(int thoiHan, String donVi) {
        String tenDonVi = switch (donVi) {
            case "NGAY" -> "ngày";
            case "NAM" -> "năm";
            default -> "tháng";
        };
        return thoiHan + " " + tenDonVi;
    }

    private static JTextArea taoOThongTin() {
        JTextArea txa = new JTextArea(3, 16);
        txa.setLineWrap(true);
        txa.setWrapStyleWord(true);
        txa.setFont(UIConfig.FONT_REGULAR);
        txa.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));
        return txa;
    }

    private void baoLoi(String tieuDe, SQLException exception) {
        JOptionPane.showMessageDialog(this, tieuDe + ":\n" + exception.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
