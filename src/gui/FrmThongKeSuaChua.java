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
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.SwingConstants;

import dao.BaoCaoSuaChuaDAO.ThongKeBieuDo;
import dao.BaoCaoSuaChuaDAO.ThongKeSuDung;
import dao.BaoCaoSuaChuaDAO.ThongKeTongQuan;
import controller.ThongKeSuaChuaController;

public class FrmThongKeSuaChua extends JPanel {
    private final AppTextField txtTuNgay = new AppTextField(12);
    private final AppTextField txtDenNgay = new AppTextField(12);
    private final AppComboBox<String> cboKySuDung = new AppComboBox<>();
    private final AppComboBox<String> cboKyBieuDo = new AppComboBox<>();
    private final AppButton btnThongKe = new AppButton("Thống kê");
    private final AppButton btnLamMoi = new AppButton("Làm mới", AppButton.Style.SECONDARY);

    private final AppLabel lblTongPhieu = new AppLabel("—", AppLabel.Style.TITLE);
    private final AppLabel lblDaHoanThanh = new AppLabel("—", AppLabel.Style.TITLE);
    private final AppLabel lblDangXuLy = new AppLabel("—", AppLabel.Style.TITLE);
    private final AppLabel lblDoanhThu = new AppLabel("—", AppLabel.Style.TITLE);
    private final BieuDoDuong pnlBieuDoTheoThang = new BieuDoDuong();
    private final BangHot pnlDichVuHot = new BangHot("DỊCH VỤ HOT", new Color(37, 99, 235));
    private final BangHot pnlPhuTungHot = new BangHot("PHỤ TÙNG HOT", new Color(22, 163, 74));
    private final AppLabel lblTrangThai = new AppLabel(
            "Chọn điều kiện rồi nhấn Thống kê", AppLabel.Style.MUTED);

    private final ThongKeSuaChuaController controller = new ThongKeSuaChuaController(this);
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
        pnlDetails.add(pnlHot, BorderLayout.CENTER);
        pnlReportContent.add(pnlDetails, BorderLayout.CENTER);
        pnlContent.add(pnlReportContent, BorderLayout.CENTER);

        JScrollPane scrNoiDung = new JScrollPane(pnlContent);
        scrNoiDung.setBorder(null);
        scrNoiDung.setOpaque(false);
        scrNoiDung.getViewport().setOpaque(false);
        scrNoiDung.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrNoiDung.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrNoiDung.getVerticalScrollBar().setUnitIncrement(16);
        add(FormUI.createPage("THỐNG KÊ SỬA CHỮA",
                "Theo dõi tình trạng phiếu sửa chữa, doanh thu và hiệu suất xử lý",
                scrNoiDung), BorderLayout.CENTER);
        add(lblTrangThai, BorderLayout.SOUTH);
        controller.khoiTao();
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

    public void capNhatTongQuan(ThongKeTongQuan tongQuan) {
        lblTongPhieu.setText(String.valueOf(tongQuan.tongPhieu()));
        lblDaHoanThanh.setText(String.valueOf(tongQuan.daHoanThanh()));
        lblDangXuLy.setText(String.valueOf(tongQuan.dangXuLy()));
        lblDoanhThu.setText(dinhDangTien(tongQuan.doanhThu()));
    }

    public void capNhatBangHot(List<ThongKeSuDung> dichVuHot, List<ThongKeSuDung> phuTungHot) {
        pnlDichVuHot.setData(dichVuHot);
        pnlPhuTungHot.setData(phuTungHot);
    }

    public void capNhatBieuDo(List<ThongKeBieuDo> danhSach) {
        String[] nhan = new String[danhSach.size()];
        double[] giaTri = new double[danhSach.size()];
        for (int i = 0; i < danhSach.size(); i++) {
            nhan[i] = danhSach.get(i).nhan();
            giaTri[i] = danhSach.get(i).soLuong();
        }
        pnlBieuDoTheoThang.setData(nhan, giaTri);
    }

    public void lamMoiBieuMau() {
        LocalDate homNay = LocalDate.now();
        txtTuNgay.setText(homNay.withDayOfMonth(1).toString());
        txtDenNgay.setText(homNay.toString());
        cboKySuDung.setSelectedItem("Tháng");
        cboKyBieuDo.setSelectedIndex(0);
        lblTongPhieu.setText("—");
        lblDaHoanThanh.setText("—");
        lblDangXuLy.setText("—");
        lblDoanhThu.setText("—");
        pnlDichVuHot.setData(List.of());
        pnlPhuTungHot.setData(List.of());
        pnlBieuDoTheoThang.setData(new String[0], new double[0]);
        lblTrangThai.setText("Đã làm mới điều kiện thống kê.");
    }

    private String dinhDangTien(BigDecimal giaTri) {
        return giaTri == null ? "0 VNĐ" : String.format("%,.0f VNĐ", giaTri);
    }

    public void hienThiLoi(String thongBao) {
        JOptionPane.showMessageDialog(this, thongBao, "Lỗi", JOptionPane.ERROR_MESSAGE);
        lblTrangThai.setText("Có lỗi khi xử lý thống kê.");
    }

    public AppButton layNutThongKe() {
        return btnThongKe;
    }

    public AppButton layNutLamMoi() {
        return btnLamMoi;
    }

    public LocalDate layTuNgay() {
        return docNgay(txtTuNgay.getText(), "Từ ngày");
    }

    public LocalDate layDenNgay() {
        return docNgay(txtDenNgay.getText(), "Đến ngày");
    }

    public String layKySuDung() {
        return (String) cboKySuDung.getSelectedItem();
    }

    public String layKyBieuDo() {
        return (String) cboKyBieuDo.getSelectedItem();
    }

    public void thongBao(String message) {
        lblTrangThai.setText(message);
    }

    private JPanel createFilterCard() {
        AppPanel pnlCard = FormUI.createCard();
        JPanel pnlFields = FormUI.createFieldsPanel();
        FormUI.addField(pnlFields, "Từ ngày", txtTuNgay, 0, 0);
        FormUI.addField(pnlFields, "Đến ngày", txtDenNgay, 2, 0);
        FormUI.addField(pnlFields, "Xếp hạng theo", cboKySuDung, 0, 1);
        FormUI.addField(pnlFields, "Biểu đồ theo", cboKyBieuDo, 2, 1);

        JPanel pnlButtons = FormUI.createButtonRow(btnThongKe, btnLamMoi);
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
