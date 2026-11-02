package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import dao.BaoCaoSuaChuaDAO;
import dao.BaoCaoSuaChuaDAO.ThongKePhieuSuaChua;
import dao.BaoCaoSuaChuaDAO.ThongKeBieuDo;
import dao.BaoCaoSuaChuaDAO.ThongKeSuDung;
import dao.BaoCaoSuaChuaDAO.ThongKeTongQuan;

public class FrmThongKeSuaChua extends JPanel {
    private final AppTextField txtTuNgay = new AppTextField(12);
    private final AppTextField txtDenNgay = new AppTextField(12);
    private final AppComboBox<String> cboKySuDung = new AppComboBox<>();
    private final AppComboBox<String> cboKyBieuDo = new AppComboBox<>();
    private final AppButton btnThongKe = new AppButton("Thống kê");
    private final AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);
    private final AppButton btnXuatBaoCao = new AppButton("Xuất báo cáo", AppButton.Style.SECONDARY);

    private final AppLabel lblTongPhieu = new AppLabel("—", AppLabel.Style.TITLE);
    private final AppLabel lblDaHoanThanh = new AppLabel("—", AppLabel.Style.TITLE);
    private final AppLabel lblDangXuLy = new AppLabel("—", AppLabel.Style.TITLE);
    private final AppLabel lblDoanhThu = new AppLabel("—", AppLabel.Style.TITLE);
    private final BieuDoDuong pnlBieuDoTheoThang = new BieuDoDuong();
    private final BangHot pnlDichVuHot = new BangHot("DỊCH VỤ HOT", new Color(37, 99, 235));
    private final BangHot pnlPhuTungHot = new BangHot("PHỤ TÙNG HOT", new Color(22, 163, 74));
    private final AppLabel lblTrangThai = new AppLabel(
            "Chọn điều kiện rồi nhấn Thống kê", AppLabel.Style.MUTED);
    private final AppLabel lblSoDong = new AppLabel("0 phiếu", AppLabel.Style.MUTED);

    private final DefaultTableModel modelThongKe = new DefaultTableModel(
            new Object[] { "Mã phiếu", "Mã xe", "Khách hàng", "Nhân viên",
                    "Ngày bắt đầu", "Trạng thái", "Tổng tiền" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable tblThongKe = FormUI.createTable(modelThongKe);

    private final BaoCaoSuaChuaDAO baoCaoSuaChuaDao = new BaoCaoSuaChuaDAO();
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ISO_LOCAL_DATE;

    public FrmThongKeSuaChua() {
        setOpaque(false);
        setLayout(new BorderLayout());

        cboKySuDung.addItem("Ngày");
        cboKySuDung.addItem("Tháng");
        cboKySuDung.addItem("Năm");
        cboKyBieuDo.addItem("Tuần");
        cboKyBieuDo.addItem("Tháng");
        cboKyBieuDo.addItem("Năm");

        LocalDate homNay = LocalDate.now();
        txtTuNgay.setText(homNay.withDayOfMonth(1).toString());
        txtDenNgay.setText(homNay.toString());
        txtTuNgay.setToolTipText("Định dạng ngày: yyyy-MM-dd");
        txtDenNgay.setToolTipText("Định dạng ngày: yyyy-MM-dd");
        ganSuKien();
        JPanel pnlContent = new JPanel(new BorderLayout(0, 14));
        pnlContent.setOpaque(false);
        pnlContent.add(createFilterCard(), BorderLayout.NORTH);

        JPanel pnlReportContent = new JPanel(new BorderLayout(0, 14));
        pnlReportContent.setOpaque(false);
        pnlReportContent.add(createSummaryCards(), BorderLayout.NORTH);

        JPanel pnlDetails = new JPanel(new BorderLayout(0, 14));
        pnlDetails.setOpaque(false);
        pnlDetails.add(createChartPlaceholders(), BorderLayout.NORTH);
        JPanel pnlHot = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHot.setOpaque(false);
        pnlHot.add(pnlDichVuHot);
        pnlHot.add(pnlPhuTungHot);
        AppScrollPane scrChiTiet = new AppScrollPane(tblThongKe);
        scrChiTiet.setPreferredSize(new Dimension(0, 230));
        scrChiTiet.setMinimumSize(new Dimension(0, 180));
        JPanel pnlDuLieu = new JPanel(new BorderLayout(0, 12));
        pnlDuLieu.setOpaque(false);
        pnlDuLieu.add(pnlHot, BorderLayout.NORTH);
        pnlDuLieu.add(FormUI.createTableCard("Chi tiết phiếu sửa chữa", lblSoDong,
                scrChiTiet), BorderLayout.CENTER);
        pnlDetails.add(pnlDuLieu, BorderLayout.CENTER);
        pnlReportContent.add(pnlDetails, BorderLayout.CENTER);
        pnlContent.add(pnlReportContent, BorderLayout.CENTER);

        JScrollPane scrNoiDung = new JScrollPane(pnlContent);
        scrNoiDung.setBorder(null);
        scrNoiDung.setOpaque(false);
        scrNoiDung.getViewport().setOpaque(false);
        scrNoiDung.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrNoiDung.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrNoiDung.setWheelScrollingEnabled(false);
        scrNoiDung.addMouseWheelListener(event -> cuonNhanh(scrNoiDung, event));
        scrNoiDung.getViewport().addMouseWheelListener(
                event -> cuonNhanh(scrNoiDung, event));
        add(FormUI.createPage("THỐNG KÊ SỬA CHỮA",
                "Theo dõi tình trạng phiếu sửa chữa, doanh thu và hiệu suất xử lý",
                scrNoiDung), BorderLayout.CENTER);
        add(lblTrangThai, BorderLayout.SOUTH);
        tblThongKe.setRowHeight(32);
        tblThongKe.setFillsViewportHeight(true);
        thongKe();
    }

    private void cuonNhanh(JScrollPane scrNoiDung, MouseWheelEvent event) {
        event.consume();
        Point viTriHienTai = scrNoiDung.getViewport().getViewPosition();
        int doCaoNoiDung = scrNoiDung.getViewport().getView().getHeight();
        int doCaoKhungNhin = scrNoiDung.getViewport().getHeight();
        int buocCuon = event.getUnitsToScroll() * 36;
        int viTriMoi = Math.max(0, Math.min(
                viTriHienTai.y + buocCuon,
                Math.max(0, doCaoNoiDung - doCaoKhungNhin)));
        scrNoiDung.getViewport().setViewPosition(
                new Point(viTriHienTai.x, viTriMoi));
    }

    private void ganSuKien() {
        btnThongKe.addActionListener(event -> thongKe());
        btnLamMoi.addActionListener(event -> lamMoi());
        btnXuatBaoCao.addActionListener(event -> xuatBaoCao());
    }

    private void thongKe() {
        try {
            LocalDate tuNgay = docNgay(txtTuNgay.getText(), "Từ ngày");
            LocalDate denNgay = docNgay(txtDenNgay.getText(), "Đến ngày");
            if (denNgay.isBefore(tuNgay)) {
                throw new IllegalArgumentException("Đến ngày phải bằng hoặc sau Từ ngày.");
            }

            ThongKeTongQuan tongQuan = baoCaoSuaChuaDao.layTongQuan(
                    tuNgay, denNgay, null, null);

            capNhatTongQuan(tongQuan);
            String kySuDung = (String) cboKySuDung.getSelectedItem();
            capNhatBangHot(pnlDichVuHot, baoCaoSuaChuaDao.layTopSuDung(
                    denNgay, kySuDung, "Dịch vụ", null, null));
            capNhatBangHot(pnlPhuTungHot, baoCaoSuaChuaDao.layTopSuDung(
                    denNgay, kySuDung, "Phụ tùng", null, null));
            capNhatBang(baoCaoSuaChuaDao.layChiTiet(tuNgay, denNgay, null, null));
            capNhatBieuDo(denNgay, null, null);
            lblTrangThai.setText("Đã cập nhật thống kê.");
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            hienThiLoi(exception.getMessage());
        } catch (Exception exception) {
            hienThiLoi("Không thể tải thống kê sửa chữa: " + exception.getMessage());
        }
    }

    private LocalDate docNgay(String giaTri, String tenTruong) {
        try {
            return LocalDate.parse(giaTri.trim(), DINH_DANG_NGAY);
        } catch (DateTimeParseException exception) {
            throw new DateTimeParseException(
                    tenTruong + " không hợp lệ. Dùng định dạng yyyy-MM-dd.",
                    giaTri, exception.getErrorIndex(), exception);
        }
    }

    private void capNhatTongQuan(ThongKeTongQuan tongQuan) {
        lblTongPhieu.setText(String.valueOf(tongQuan.tongPhieu()));
        lblDaHoanThanh.setText(String.valueOf(tongQuan.daHoanThanh()));
        lblDangXuLy.setText(String.valueOf(tongQuan.dangXuLy()));
        lblDoanhThu.setText(dinhDangTien(tongQuan.doanhThu()));
    }

    private void capNhatBangHot(BangHot bangHot, List<ThongKeSuDung> danhSach) {
        bangHot.setData(danhSach);
    }

    private void capNhatBang(List<ThongKePhieuSuaChua> danhSach) {
        modelThongKe.setRowCount(0);
        for (ThongKePhieuSuaChua phieu : danhSach) {
            modelThongKe.addRow(new Object[] {
                    phieu.maPhieuSuaChua(),
                    phieu.maXe(),
                    phieu.tenKhachHang(),
                    phieu.tenNhanVien(),
                    phieu.ngayBatDau(),
                    phieu.trangThai(),
                    dinhDangTien(phieu.tongTien())
            });
        }
        lblSoDong.setText(danhSach.size() + " phiếu");
    }

    private void capNhatBieuDo(LocalDate denNgay, String trangThai, String maNV) throws SQLException {
        List<ThongKeBieuDo> danhSach = baoCaoSuaChuaDao.layTheoBieuDo(
                denNgay, (String) cboKyBieuDo.getSelectedItem(), trangThai, maNV);
        String[] nhan = new String[danhSach.size()];
        double[] giaTri = new double[danhSach.size()];
        for (int i = 0; i < danhSach.size(); i++) {
            nhan[i] = danhSach.get(i).nhan();
            giaTri[i] = danhSach.get(i).soLuong();
        }
        pnlBieuDoTheoThang.setData(nhan, giaTri);
    }

    private void lamMoi() {
        LocalDate homNay = LocalDate.now();
        txtTuNgay.setText(homNay.withDayOfMonth(1).toString());
        txtDenNgay.setText(homNay.toString());
        cboKySuDung.setSelectedIndex(0);
        modelThongKe.setRowCount(0);
        lblSoDong.setText("0 phiếu");
        lblTongPhieu.setText("—");
        lblDaHoanThanh.setText("—");
        lblDangXuLy.setText("—");
        lblDoanhThu.setText("—");
        pnlDichVuHot.setData(List.of());
        pnlPhuTungHot.setData(List.of());
        pnlBieuDoTheoThang.setData(new String[0], new double[0]);
        lblTrangThai.setText("Đã làm mới điều kiện thống kê.");
    }

    private void xuatBaoCao() {
        if (modelThongKe.getRowCount() == 0) {
            hienThiLoi("Chưa có dữ liệu để xuất. Vui lòng thực hiện thống kê trước.");
            return;
        }

        JFileChooser boChonTep = new JFileChooser();
        boChonTep.setDialogTitle("Lưu báo cáo sửa chữa");
        boChonTep.setSelectedFile(new java.io.File("bao-cao-sua-chua.csv"));
        boChonTep.setFileFilter(new FileNameExtensionFilter("Tệp CSV (*.csv)", "csv"));
        if (boChonTep.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path duongDan = boChonTep.getSelectedFile().toPath();
        try (BufferedWriter writer = Files.newBufferedWriter(duongDan, StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            for (int column = 0; column < modelThongKe.getColumnCount(); column++) {
                if (column > 0) {
                    writer.write(",");
                }
                writer.write("\"" + modelThongKe.getColumnName(column) + "\"");
            }
            writer.newLine();
            for (int row = 0; row < modelThongKe.getRowCount(); row++) {
                for (int column = 0; column < modelThongKe.getColumnCount(); column++) {
                    if (column > 0) {
                        writer.write(",");
                    }
                    writer.write("\"" + String.valueOf(modelThongKe.getValueAt(row, column))
                            .replace("\"", "\"\"") + "\"");
                }
                writer.newLine();
            }
            lblTrangThai.setText("Đã xuất báo cáo: " + duongDan);
        } catch (IOException exception) {
            hienThiLoi("Không thể xuất báo cáo: " + exception.getMessage());
        }
    }

    private String dinhDangTien(BigDecimal giaTri) {
        return giaTri == null ? "0 VNĐ" : String.format("%,.0f VNĐ", giaTri);
    }

    private void hienThiLoi(String thongBao) {
        JOptionPane.showMessageDialog(this, thongBao, "Lỗi", JOptionPane.ERROR_MESSAGE);
        lblTrangThai.setText("Có lỗi khi xử lý thống kê.");
    }

    private JPanel createFilterCard() {
        AppPanel pnlCard = FormUI.createCard();
        JPanel pnlFields = FormUI.createFieldsPanel();
        FormUI.addField(pnlFields, "Từ ngày", txtTuNgay, 0, 0);
        FormUI.addField(pnlFields, "Đến ngày", txtDenNgay, 2, 0);
        FormUI.addField(pnlFields, "Xếp hạng theo", cboKySuDung, 0, 1);
        FormUI.addField(pnlFields, "Biểu đồ theo", cboKyBieuDo, 2, 1);

        JPanel pnlButtons = FormUI.createButtonRow(btnThongKe, btnLamMoi, btnXuatBaoCao);
        pnlCard.add(FormUI.createSectionTitle("Điều kiện thống kê"), BorderLayout.NORTH);
        pnlCard.add(pnlFields, BorderLayout.CENTER);
        pnlCard.add(pnlButtons, BorderLayout.SOUTH);
        return pnlCard;
    }

    private JPanel createSummaryCards() {
        JPanel pnlSummary = new JPanel(new GridLayout(1, 1));
        pnlSummary.setOpaque(false);

        JPanel pnlTongQuan = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlTongQuan.setOpaque(false);
        pnlTongQuan.add(createMetricCard("Tổng phiếu sửa chữa", lblTongPhieu));
        pnlTongQuan.add(createMetricCard("Đã hoàn thành", lblDaHoanThanh));
        pnlTongQuan.add(createMetricCard("Đang xử lý", lblDangXuLy));
        pnlTongQuan.add(createMetricCard("Tổng doanh thu", lblDoanhThu));

        pnlSummary.add(pnlTongQuan);
        return pnlSummary;
    }

    private JPanel createMetricCard(String title, AppLabel lblValue) {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.add(new AppLabel(title, AppLabel.Style.MUTED), BorderLayout.NORTH);
        pnlCard.add(lblValue, BorderLayout.CENTER);
        return pnlCard;
    }

    private JPanel createChartPlaceholders() {
        JPanel pnlCharts = new JPanel(new BorderLayout());
        pnlCharts.setOpaque(false);
        pnlCharts.add(createChartCard(
                "Số phiếu sửa chữa theo tuần, tháng, năm",
                pnlBieuDoTheoThang), BorderLayout.CENTER);
        return pnlCharts;
    }

    private JPanel createChartCard(String title, JComponent chart) {
        AppPanel pnlCard = FormUI.createCard();
        pnlCard.setPreferredSize(new Dimension(0, 250));
        pnlCard.add(FormUI.createSectionTitle(title));
        pnlCard.add(chart, BorderLayout.CENTER);
        return pnlCard;
    }

    private static final class BangHot extends AppPanel {
        private final String tieuDe;
        private final Color mauChu;
        private final JPanel pnlNoiDung = new JPanel();

        private BangHot(String tieuDe, Color mauChu) {
            super(new BorderLayout());
            this.tieuDe = tieuDe;
            this.mauChu = mauChu;
            setLayout(new BorderLayout(0, 8));
            setOpaque(true);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(221, 231, 245)),
                    BorderFactory.createEmptyBorder(14, 16, 14, 16)));
            add(new AppLabel(tieuDe, AppLabel.Style.SECTION), BorderLayout.NORTH);
            pnlNoiDung.setOpaque(false);
            pnlNoiDung.setLayout(new BoxLayout(pnlNoiDung, BoxLayout.Y_AXIS));
            add(pnlNoiDung, BorderLayout.CENTER);
            setPreferredSize(new Dimension(0, 190));
            setData(List.of());
        }

        private void setData(List<ThongKeSuDung> danhSach) {
            pnlNoiDung.removeAll();
            if (danhSach.isEmpty()) {
                JLabel lblRong = new JLabel("Chưa có dữ liệu");
                lblRong.setForeground(UIConfig.COLOR_MUTED);
                pnlNoiDung.add(lblRong);
            } else {
                int[] coChu = { 22, 18, 15 };
                for (int i = 0; i < Math.min(3, danhSach.size()); i++) {
                    ThongKeSuDung item = danhSach.get(i);
                    JLabel lblItem = new JLabel(
                            (i + 1) + ". " + item.ten() + "  (" + item.soLuong() + ")",
                            SwingConstants.LEFT);
                    lblItem.setToolTipText(item.ma() + " - " + item.ten());
                    lblItem.setAlignmentX(Component.LEFT_ALIGNMENT);
                    lblItem.setFont(lblItem.getFont().deriveFont(Font.BOLD, (float) coChu[i]));
                    lblItem.setForeground(i == 0 ? mauChu : UIConfig.COLOR_TEXT);
                    lblItem.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                    pnlNoiDung.add(lblItem);
                    if (i < Math.min(3, danhSach.size()) - 1) {
                        pnlNoiDung.add(Box.createVerticalStrut(3));
                    }
                }
            }
            pnlNoiDung.revalidate();
            pnlNoiDung.repaint();
        }
    }

    private static final class BieuDoDuong extends JComponent {
        private String[] nhan = new String[0];
        private double[] giaTri = new double[0];

        private BieuDoDuong() {
            setOpaque(true);
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(500, 190));
        }

        private void setData(String[] nhan, double[] giaTri) {
            this.nhan = Arrays.copyOf(nhan, nhan.length);
            this.giaTri = Arrays.copyOf(giaTri, giaTri.length);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (giaTri.length == 0 || Arrays.stream(giaTri).sum() <= 0) {
                    veThongBaoRong(g2);
                    return;
                }

                int paddingLeft = 78;
                int paddingRight = 64;
                int paddingTop = 24;
                int paddingBottom = 70;

                int chartWidth = getWidth() - paddingLeft - paddingRight;
                int chartHeight = getHeight() - paddingTop - paddingBottom;
                double giaTriLonNhat = Arrays.stream(giaTri).max().orElse(0);
                double mocLonNhat = giaTriLonNhat <= 0 ? 1 : giaTriLonNhat;

                veDuongLuoi(g2, paddingLeft, paddingTop, chartWidth, chartHeight, mocLonNhat);

                int[] toaDoX = new int[giaTri.length];
                int[] toaDoY = new int[giaTri.length];

                for (int i = 0; i < giaTri.length; i++) {
                    if (giaTri.length == 1) {
                        toaDoX[i] = paddingLeft + chartWidth / 2;
                    } else {
                        toaDoX[i] = paddingLeft + i * chartWidth / (giaTri.length - 1);
                    }
                    toaDoY[i] = paddingTop + chartHeight
                            - (int) Math.round(giaTri[i] / mocLonNhat * chartHeight);
                }

                g2.setColor(UIConfig.COLOR_PRIMARY);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 1; i < giaTri.length; i++) {
                    g2.drawLine(toaDoX[i - 1], toaDoY[i - 1], toaDoX[i], toaDoY[i]);
                }

                g2.setStroke(new BasicStroke(1.6f));
                for (int i = 0; i < giaTri.length; i++) {
                    g2.setColor(UIConfig.COLOR_PRIMARY);
                    g2.fillOval(toaDoX[i] - 5, toaDoY[i] - 5, 10, 10);

                    String giaTriHienTai = String.valueOf((int) giaTri[i]);
                    int chieuRongGiaTri = g2.getFontMetrics().stringWidth(giaTriHienTai);
                    g2.setColor(UIConfig.COLOR_TEXT);
                    g2.drawString(giaTriHienTai, toaDoX[i] - chieuRongGiaTri / 2, toaDoY[i] - 12);

                    int buocNhan = Math.max(1, (int) Math.ceil((double) nhan.length / 8));
                    if (i % buocNhan == 0 || i == nhan.length - 1) {
                        String nhanHienTai = rutGonNhan(nhan[i]);
                        int chieuRongNhan = g2.getFontMetrics().stringWidth(nhanHienTai);
                        g2.drawString(nhanHienTai, toaDoX[i] - chieuRongNhan / 2, getHeight() - 22);
                    }
                }

                g2.setColor(UIConfig.COLOR_TEXT);
                String nhanTrucX = "Mốc thời gian";
                int chieuRongTrucX = g2.getFontMetrics().stringWidth(nhanTrucX);
                g2.drawString(nhanTrucX,
                        paddingLeft + (chartWidth - chieuRongTrucX) / 2,
                        getHeight() - 2);

                String nhanTrucY = "Số phiếu sửa chữa";
                int chieuRongTrucY = g2.getFontMetrics().stringWidth(nhanTrucY);
                g2.translate(19, paddingTop + chartHeight / 2.0);
                g2.rotate(-Math.PI / 2);
                g2.drawString(nhanTrucY, -chieuRongTrucY / 2, 0);
            } finally {
                g2.dispose();
            }
        }

        private String rutGonNhan(String nhan) {
            if (nhan != null && nhan.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return nhan.substring(8) + "/" + nhan.substring(5, 7);
            }
            return nhan;
        }

        private void veDuongLuoi(Graphics2D g2, int paddingLeft, int paddingTop, int chartWidth, int chartHeight,
                double mocLonNhat) {
            int soDuongLuoi = 4;
            for (int i = 0; i <= soDuongLuoi; i++) {
                int y = paddingTop + chartHeight - i * chartHeight / soDuongLuoi;
                double giaTriThuc = mocLonNhat * i / soDuongLuoi;
                g2.setColor(UIConfig.COLOR_TABLE_GRID);
                g2.drawLine(paddingLeft, y, paddingLeft + chartWidth, y);
                g2.setColor(UIConfig.COLOR_MUTED);
                String nhanTruc = String.valueOf((int) giaTriThuc);
                int doRong = g2.getFontMetrics().stringWidth(nhanTruc);
                g2.drawString(nhanTruc, paddingLeft - doRong - 10, y + 4);
            }
            g2.setColor(UIConfig.COLOR_BORDER);
            g2.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + chartHeight);
            g2.drawLine(paddingLeft, paddingTop + chartHeight, paddingLeft + chartWidth, paddingTop + chartHeight);
        }

        private void veThongBaoRong(Graphics2D g2) {
            String thongBao = "Chưa có dữ liệu";
            FontMetrics metrics = g2.getFontMetrics();
            g2.setColor(UIConfig.COLOR_MUTED);
            g2.drawString(thongBao, (getWidth() - metrics.stringWidth(thongBao)) / 2, getHeight() / 2);
        }
    }
}
