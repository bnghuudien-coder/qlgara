package gui;

import dao.ChinhSachBaoHanhDAO;
import dao.ChinhSachBaoHanhDAO.PhuTungBaoHanh;
import dao.HoaDonDAO;
import dao.HoaDonDAO.DongChiTiet;
import dao.HoaDonDAO.KhuyenMai;
import dao.HoaDonDAO.LichSuHoaDon;
import dao.HoaDonDAO.Phieu;
import dao.HoaDonDAO.PhuTung;
import dao.HoaDonDAO.ThongTinHoaDon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class FrmHoaDon extends JPanel {
    private static final String SUA_CHUA = "Hóa đơn sửa chữa";
    private static final String MUA_PHU_TUNG = "Hóa đơn mua phụ tùng";
    private static final String DICH_VU = "Dịch vụ";
    private static final String PHU_TUNG = "Phụ tùng";
    private static final String CHUA_THANH_TOAN = "Chưa thanh toán";
    private static final String THE_TRONG = "trong";
    private static final String THE_SUA_CHUA = "suachua";
    private static final String THE_PHU_TUNG = "phutung";
    private static final String MSG_CHON_PHIEU = "Chọn phiếu sửa chữa để kiểm tra bảo hành.";
    private static final String MSG_KHONG_BAO_HANH = "Phiếu này không có phụ tùng thuộc chính sách đang áp dụng.";
    private static final String MSG_HD_MUA_PHU_TUNG = "Hóa đơn mua phụ tùng không có phiếu sửa chữa.";
    private static final BigDecimal VAT = new BigDecimal("0.08");
    private static final Color MAU_CHU = new Color(40, 50, 65);
    private static final Color MAU_PHU = new Color(100, 112, 130);
    private static final Color MAU_NEN_TRANG = new Color(244, 247, 251);
    private static final Color MAU_NEN_O = new Color(247, 249, 252);
    private static final Color MAU_NEN_TIEU_DE = new Color(240, 245, 255);
    private static final Color MAU_DONG_CHAN = new Color(246, 248, 252);
    private static final Color MAU_CHON = new Color(219, 234, 254);
    private static final Color MAU_NUT = new Color(37, 99, 235);
    private static final Color MAU_NUT_DAM = new Color(29, 78, 216);
    private static final Color MAU_VIEN = new Color(220, 225, 232);
    private static final Color MAU_DO = new Color(220, 53, 69);
    private static final Color MAU_DO_DAM = new Color(185, 28, 40);
    private static final Color MAU_XANH_LA = new Color(22, 130, 70);
    private static final Color MAU_XANH_LA_DAM = new Color(17, 105, 55);
    private static final Font FONT_THUONG = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_DAM = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_NHO_DAM = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_GIA_TRI = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_NHAN_TONG = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_TONG = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_TIEU_DE = new Font("Segoe UI", Font.BOLD, 24);
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat DINH_DANG_TIEN = NumberFormat
            .getIntegerInstance(Locale.forLanguageTag("vi-VN"));
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final ChinhSachBaoHanhDAO chinhSachBaoHanhDAO = new ChinhSachBaoHanhDAO();

    // Thông tin hóa đơn (nhân viên là người đang đăng nhập, dùng cho cả hai loại)
    private final JTextField txtMaHoaDon = new JTextField("Tự động");
    private final JTextField txtNgayLap = new JTextField(LocalDate.now().format(DINH_DANG_NGAY));
    private final JTextField txtMaNhanVienLapHoaDon = new JTextField();
    private final JTextField txtTenNhanVienLapHoaDon = new JTextField();
    private final JComboBox<String> cboLoaiHoaDon = new JComboBox<>(new String[] {
            "-- Chọn loại hóa đơn --", SUA_CHUA, MUA_PHU_TUNG });

    // Phiếu sửa chữa
    private final JTextField txtMaPhieuSuaChua = new JTextField();
    private final JTextField txtMaKhachHang = new JTextField();
    private final JTextField txtTenKhachHang = new JTextField();
    private final JTextField txtSoDienThoai = new JTextField();
    private final JTextField txtBienSoXe = new JTextField();
    private final JTextField txtTenXe = new JTextField();
    // Các ô tự điền từ phiếu (chỉ đọc).
    private final JTextField[] oTuDien = { txtMaKhachHang, txtTenKhachHang, txtSoDienThoai, txtBienSoXe, txtTenXe };
    private final JButton btnTimPhieu = taoNutPhu("Tìm phiếu");

    // Phụ tùng và khuyến mãi
    private final JComboBox<PhuTung> cboPhuTung = new JComboBox<>();
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
    private final JTextField txtDonGia = new JTextField("0");
    private final JComboBox<KhuyenMai> cboKhuyenMai = new JComboBox<>();

    // Tổng kết
    private final JLabel lblTienDichVu = new JLabel("0 đ");
    private final JLabel lblTienPhuTung = new JLabel("0 đ");
    private final JLabel lblTongHoaDon = new JLabel("0 đ");
    private final JLabel lblTienVAT = new JLabel("0 đ");
    private final JLabel lblTienGiam = new JLabel("0 đ");
    private final JLabel lblTongThanhToan = new JLabel("0 đ");
    private BigDecimal tienVAT = BigDecimal.ZERO;
    private BigDecimal tienGiam = BigDecimal.ZERO;
    private BigDecimal tongThanhToan = BigDecimal.ZERO;

    // Trạng thái
    // Mã hóa đơn vừa lưu thành công, dùng cho nút Thanh toán.
    private String maHoaDonVuaLuu;
    // Đang đổ dữ liệu hóa đơn cũ vào form: tạm tắt các tính toán/xóa tự động.
    private boolean dangNapHoaDonCu;
    // Form đang hiển thị một hóa đơn đã lưu (không cho lưu lại).
    private boolean dangXemHoaDonCu;
    private boolean dangDongBoLichSu;
    // Khuyến mãi đã ngừng áp dụng, thêm tạm vào combo khi xem hóa đơn cũ.
    private KhuyenMai khuyenMaiNgungApDung;

    // Khu vực "Nguồn hóa đơn"
    private final CardLayout cardNguon = new CardTheoThe();
    private final JPanel pnlNguon = new JPanel(cardNguon);
    private The theNguon;

    // Bảng chi tiết: Loại, Mã, Tên, Số lượng, Đơn giá (BigDecimal), Thành tiền
    // (BigDecimal)
    private final DefaultTableModel modelChiTiet = new MoHinhBang(
            "Loại", "Mã", "Tên dịch vụ / phụ tùng", "Số lượng", "Đơn giá", "Thành tiền");
    private final JTable tblChiTiet = new BangSoc(modelChiTiet);

    // Bảng bảo hành
    private final DefaultTableModel modelBaoHanh = new MoHinhBang(
            "Phụ tùng", "Số lượng", "Chính sách", "Thời hạn bảo hành");
    private final List<PhuTungBaoHanh> dsBaoHanhHienThi = new ArrayList<>();
    private final JTable tblBaoHanh = new JTable(modelBaoHanh) {
        @Override
        public String getToolTipText(MouseEvent event) {
            int row = rowAtPoint(event.getPoint());
            if (row < 0) {
                return null;
            }
            int modelRow = convertRowIndexToModel(row);
            return modelRow < dsBaoHanhHienThi.size()
                    ? taoThongTinBaoHanhTooltip(dsBaoHanhHienThi.get(modelRow))
                    : null;
        }
    };
    private final JLabel lblTinhTrangBaoHanh = new JLabel(MSG_CHON_PHIEU);

    // Lịch sử hóa đơn
    private final DefaultTableModel modelLichSu = new MoHinhBang(
            "Mã hóa đơn", "Ngày lập", "Mã NV", "Mã phiếu sửa chữa", "Tổng thanh toán", "Trạng thái");
    private final JTable tblLichSu = new BangSoc(modelLichSu);
    private final JLabel lblSoLichSu = new JLabel("0 hóa đơn");
    private final JButton btnThemHoaDon = taoNut("Thêm");

    // Dữ liệu trả về từ luồng nền
    private record DuLieuKhoiTao(String maHoaDon, List<PhuTung> dsPhuTung,
            List<KhuyenMai> dsKhuyenMai, List<LichSuHoaDon> dsLichSu) {
    }

    private record HoaDonDaLuu(ThongTinHoaDon hd, List<DongChiTiet> dsChiTiet,
            List<PhuTungBaoHanh> dsBaoHanh) {
    }

    // KHỞI TẠO
    public FrmHoaDon() {
        setLayout(new BorderLayout(0, 12));
        setBackground(MAU_NEN_TRANG);
        setBorder(new EmptyBorder(14, 18, 14, 18));
        kieuCacOComponent();
        cauHinhBangChiTiet();
        cauHinhBangBaoHanh();
        cauHinhBangLichSu();
        add(taoTieuDe(), BorderLayout.NORTH);
        add(taoVungCuon(), BorderLayout.CENTER);
        caiDatSuKien();
        capNhatLoaiHoaDon();
    }

    public void setNhanVienLapHoaDon(String maNV, String tenNV) {
        txtMaNhanVienLapHoaDon.setText(nz(maNV).trim());
        txtTenNhanVienLapHoaDon.setText(nz(tenNV).trim());
    }

    public void taiDuLieuBanDauAsync() {
        chayNen(() -> new DuLieuKhoiTao(
                hoaDonDAO.taoMaHoaDonMoi(),
                hoaDonDAO.layDanhSachPhuTung(),
                hoaDonDAO.layKhuyenMaiDangApDung(),
                hoaDonDAO.layLichSuHoaDon()),
                duLieu -> {
                    txtMaHoaDon.setText(duLieu.maHoaDon().isEmpty() ? "Tự động" : duLieu.maHoaDon());

                    cboPhuTung.removeAllItems();
                    cboPhuTung.addItem(new PhuTung("", "-- Chọn phụ tùng --", BigDecimal.ZERO));
                    duLieu.dsPhuTung().forEach(cboPhuTung::addItem);

                    cboKhuyenMai.removeAllItems();
                    cboKhuyenMai.addItem(new KhuyenMai("", "-- Không áp dụng khuyến mãi --", BigDecimal.ZERO));
                    duLieu.dsKhuyenMai().forEach(cboKhuyenMai::addItem);
                    cboKhuyenMai.setSelectedIndex(0);

                    modelLichSu.setRowCount(0);
                    duLieu.dsLichSu().forEach(h -> modelLichSu.addRow(dongLichSu(h)));
                },
                "Không thể tải dữ liệu ban đầu của hóa đơn");
    }

    // DỰNG GIAO DIỆN
    // Khung chung
    private JScrollPane taoVungCuon() {
        // Ẩn thanh cuộn nhưng vẫn cuộn được bằng con lăn chuột
        JScrollPane sp = new JScrollPane(taoNoiDung(),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(24);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        return sp;
    }

    private JPanel taoTieuDe() {
        JLabel lblTieuDe = new JLabel("LẬP HÓA ĐƠN", SwingConstants.CENTER);
        lblTieuDe.setFont(FONT_TIEU_DE);
        lblTieuDe.setForeground(MAU_CHU);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnThemHoaDon);

        // Khối trống bên trái cùng bề rộng với cụm nút để tiêu đề nằm chính giữa
        JPanel pnlCanBang = new JPanel();
        pnlCanBang.setOpaque(false);
        pnlCanBang.setPreferredSize(pnlNut.getPreferredSize());

        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(0, 0, 4, 0));
        pnl.add(pnlCanBang, BorderLayout.WEST);
        pnl.add(lblTieuDe, BorderLayout.CENTER);
        pnl.add(pnlNut, BorderLayout.EAST);
        return pnl;
    }

    private JPanel taoNoiDung() {
        JPanel pnl = new TrangCuon();
        pnl.setLayout(new GridBagLayout());
        pnl.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.gridy = 0;
        g.weighty = 1;

        g.gridx = 0;
        g.weightx = 0.35;
        g.insets = new Insets(0, 0, 0, 14);
        pnl.add(taoCotTrai(), g);

        g.gridx = 1;
        g.weightx = 0.65;
        g.insets = new Insets(0, 0, 0, 0);
        pnl.add(taoCotPhai(), g);

        // Lịch sử hóa đơn nằm dưới hai cột, chiếm cả bề ngang
        g.gridx = 0;
        g.gridy = 1;
        g.gridwidth = 2;
        g.weightx = 1;
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(12, 0, 0, 0);
        pnl.add(taoTheLichSu(), g);
        return pnl;
    }

    private JPanel taoCotTrai() {
        JPanel col = new CotNoiDung(430, 380);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.anchor = GridBagConstraints.NORTH;

        g.gridy = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        col.add(taoTheThongTinHoaDon(), g);

        // Thẻ Nguồn hóa đơn chiếm hết chiều cao còn lại -> đáy bằng khung Tổng kết
        g.gridy = 1;
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(12, 0, 0, 0);
        col.add(taoTheNguon(), g);
        return col;
    }

    private JPanel taoCotPhai() {
        JPanel col = new CotNoiDung(760, 560);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;

        g.gridy = 0;
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;
        col.add(taoTheChiTiet(), g);

        g.gridy = 1;
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(12, 0, 0, 0);
        col.add(taoTheTongKet(), g);
        return col;
    }

    private The taoTheThongTinHoaDon() {
        The the = new The("THÔNG TIN HÓA ĐƠN");
        themDongKep(the.noiDung, 0, "Mã hóa đơn:", txtMaHoaDon, 0.4, "Ngày lập:", txtNgayLap, 0.6);
        themDongKep(the.noiDung, 1, "Mã NV:", txtMaNhanVienLapHoaDon, 0.4, "Tên NV:", txtTenNhanVienLapHoaDon, 0.6);
        themDong(the.noiDung, 2, "Loại hóa đơn:", cboLoaiHoaDon, 92);
        return the;
    }

    private The taoTheNguon() {
        theNguon = new The("NGUỒN HÓA ĐƠN");
        theNguon.noiDung.setLayout(new BorderLayout());

        pnlNguon.setOpaque(false);
        pnlNguon.add(taoTheTrong(), THE_TRONG);
        pnlNguon.add(neoTren(taoTheSuaChua()), THE_SUA_CHUA);
        pnlNguon.add(neoTren(taoThePhuTung()), THE_PHU_TUNG);

        theNguon.noiDung.add(pnlNguon, BorderLayout.CENTER);
        return theNguon;
    }

    // Hiển thị khi chưa chọn loại hóa đơn.
    private JPanel taoTheTrong() {
        JLabel lbl = new JLabel(
                "<html><div style='text-align:center'>Chọn loại hóa đơn ở trên<br>"
                        + "để bắt đầu lập hóa đơn.</div></html>",
                SwingConstants.CENTER);
        lbl.setFont(FONT_THUONG);
        lbl.setForeground(MAU_PHU);

        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setPreferredSize(new Dimension(300, 120));
        pnl.add(lbl, BorderLayout.CENTER);
        return pnl;
    }

    // Hóa đơn sửa chữa: nhập mã phiếu, hệ thống tự điền khách hàng và xe.
    private JPanel taoTheSuaChua() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);

        JPanel dongMaPhieu = new JPanel(new BorderLayout(8, 0));
        dongMaPhieu.setOpaque(false);
        dongMaPhieu.add(txtMaPhieuSuaChua, BorderLayout.CENTER);
        dongMaPhieu.add(btnTimPhieu, BorderLayout.EAST);

        themDong(pnl, 0, "Mã phiếu:", dongMaPhieu);
        themTieuMuc(pnl, 1, "KHÁCH HÀNG VÀ XE");
        themDong(pnl, 2, "Mã khách hàng:", txtMaKhachHang);
        themDong(pnl, 3, "Tên khách hàng:", txtTenKhachHang);
        themDong(pnl, 4, "Số điện thoại:", txtSoDienThoai);
        themDong(pnl, 5, "Biển số xe:", txtBienSoXe);
        themDong(pnl, 6, "Tên xe:", txtTenXe);
        return pnl;
    }

    // Hóa đơn mua phụ tùng: chọn phụ tùng, số lượng, đơn giá rồi thêm vào bảng.
    private JPanel taoThePhuTung() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);

        themDong(pnl, 0, "Phụ tùng:", cboPhuTung);
        themDong(pnl, 1, "Số lượng:", spnSoLuong);
        themDong(pnl, 2, "Đơn giá:", txtDonGia);

        JButton btnThem = taoNut("Thêm vào hóa đơn");
        btnThem.addActionListener(e -> themPhuTung());

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 3;
        g.gridwidth = 2;
        g.weightx = 1;
        g.anchor = GridBagConstraints.EAST;
        g.insets = new Insets(10, 0, 0, 0);
        pnl.add(btnThem, g);
        return pnl;
    }

    private JPanel neoTren(JPanel p) {
        JPanel w = new JPanel(new BorderLayout());
        w.setOpaque(false);
        w.add(p, BorderLayout.NORTH);
        return w;
    }

    private The taoTheChiTiet() {
        The the = new The("CHI TIẾT HÓA ĐƠN");
        the.noiDung.setLayout(new BorderLayout(0, 10));

        JScrollPane scroll = new JScrollPane(tblChiTiet);
        kieuKhungCuon(scroll, 500, 120, 300, 100);

        JButton btnLamMoi = taoNutPhu("Làm mới nhập liệu");
        JButton btnXoaDong = taoNutMau("Xóa dòng", MAU_DO, MAU_DO_DAM, Color.WHITE, MAU_DO_DAM);
        btnLamMoi.addActionListener(e -> lamMoiNhapLieu());
        btnXoaDong.addActionListener(e -> xoaDongChiTiet());

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnLamMoi);
        pnlNut.add(btnXoaDong);

        JPanel pnlGiua = new JPanel(new BorderLayout(0, 10));
        pnlGiua.setOpaque(false);
        pnlGiua.add(scroll, BorderLayout.CENTER);
        pnlGiua.add(taoTheBaoHanh(), BorderLayout.SOUTH);

        the.noiDung.add(pnlGiua, BorderLayout.CENTER);
        the.noiDung.add(pnlNut, BorderLayout.SOUTH);
        return the;
    }

    private JPanel taoTheBaoHanh() {
        JLabel lblTieuDe = new JLabel("PHỤ TÙNG ĐƯỢC BẢO HÀNH");
        lblTieuDe.setFont(FONT_DAM);
        lblTieuDe.setForeground(MAU_NUT_DAM);

        lblTinhTrangBaoHanh.setFont(FONT_THUONG);
        lblTinhTrangBaoHanh.setForeground(MAU_PHU);
        lblTinhTrangBaoHanh.setToolTipText("Thời hạn bảo hành được tính từ ngày lập hóa đơn.");

        JPanel pnlTieuDe = new JPanel(new BorderLayout(8, 0));
        pnlTieuDe.setOpaque(false);
        pnlTieuDe.add(lblTieuDe, BorderLayout.WEST);
        pnlTieuDe.add(lblTinhTrangBaoHanh, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(tblBaoHanh);
        kieuKhungCuon(scroll, 500, 112, 300, 80);

        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setOpaque(false);
        pnl.add(pnlTieuDe, BorderLayout.NORTH);
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    // Tổng kết
    private The taoTheTongKet() {
        The the = new The("TỔNG KẾT HÓA ĐƠN");
        the.noiDung.setLayout(new BorderLayout(0, 12));
        the.noiDung.setBorder(new EmptyBorder(6, 0, 12, 12));

        JPanel pnlHaiNua = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHaiNua.setOpaque(false);
        pnlHaiNua.add(taoNuaTraiTongKet());
        pnlHaiNua.add(taoNuaPhaiTongKet());

        the.noiDung.add(pnlHaiNua, BorderLayout.CENTER);
        the.noiDung.add(taoNutThaoTac(), BorderLayout.SOUTH);
        return the;
    }

    // Nửa trái: tiền dịch vụ, tiền phụ tùng và khuyến mãi.
    private JPanel taoNuaTraiTongKet() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, MAU_VIEN),
                new EmptyBorder(0, 0, 0, 12)));
        themDongTien(pnl, 0, "Tiền dịch vụ:", lblTienDichVu);
        themDongTien(pnl, 1, "Tiền phụ tùng:", lblTienPhuTung);

        JLabel lblKM = new JLabel("Khuyến mãi:");
        lblKM.setFont(FONT_THUONG);
        lblKM.setForeground(MAU_PHU);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.gridy = 2;
        g.insets = new Insets(10, 12, 2, 0);
        pnl.add(lblKM, g);
        g.gridy = 3;
        g.insets = new Insets(2, 12, 6, 0);
        pnl.add(cboKhuyenMai, g);
        return pnl;
    }

    // Nửa phải: tổng hóa đơn, thuế, giảm giá và tổng thanh toán.
    private JPanel taoNuaPhaiTongKet() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        themDongTien(pnl, 0, "Tổng hóa đơn (đã gồm VAT):", lblTongHoaDon);
        themDongTien(pnl, 1, "Tiền thuế (VAT 8%):", lblTienVAT);
        themDongTien(pnl, 2, "Tiền giảm:", lblTienGiam);

        GridBagConstraints gs = new GridBagConstraints();
        gs.gridy = 3;
        gs.gridwidth = 2;
        gs.weightx = 1;
        gs.fill = GridBagConstraints.HORIZONTAL;
        gs.insets = new Insets(6, 12, 6, 0);
        pnl.add(new JSeparator(), gs);

        JLabel lblNhanTong = new JLabel("Tổng thanh toán:");
        lblNhanTong.setFont(FONT_NHAN_TONG);
        lblNhanTong.setForeground(MAU_CHU);

        GridBagConstraints gt = new GridBagConstraints();
        gt.gridy = 4;
        gt.gridx = 0;
        gt.weightx = 1;
        gt.anchor = GridBagConstraints.WEST;
        gt.insets = new Insets(4, 12, 4, 8);
        pnl.add(lblNhanTong, gt);

        gt.gridx = 1;
        gt.weightx = 0;
        gt.anchor = GridBagConstraints.EAST;
        gt.insets = new Insets(4, 0, 4, 0);
        pnl.add(lblTongThanhToan, gt);
        return pnl;
    }

    private JPanel taoNutThaoTac() {
        JButton btnLamMoi = taoNutPhu("Làm mới");
        JButton btnLuu = taoNut("Lưu hóa đơn");
        JButton btnThanhToan = taoNutMau("Thanh toán", MAU_XANH_LA, MAU_XANH_LA_DAM, Color.WHITE, MAU_XANH_LA);

        btnLamMoi.addActionListener(e -> {
            int chon = JOptionPane.showConfirmDialog(this,
                    "Bạn có muốn làm mới dữ liệu đang nhập không?",
                    "Xác nhận làm mới", JOptionPane.YES_NO_OPTION);
            if (chon == JOptionPane.YES_OPTION) {
                lamMoiToanBo();
            }
        });
        btnLuu.addActionListener(e -> luuHoaDon());
        btnThanhToan.addActionListener(e -> moFormThanhToan());

        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnl.setOpaque(false);
        pnl.add(btnLamMoi);
        pnl.add(btnLuu);
        pnl.add(btnThanhToan);
        return pnl;
    }

    // Lịch sử hóa đơn
    private The taoTheLichSu() {
        The the = new The("LỊCH SỬ HÓA ĐƠN");
        the.themBenPhaiTieuDe(lblSoLichSu);
        the.noiDung.setLayout(new BorderLayout());

        JScrollPane scroll = new JScrollPane(tblLichSu,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        kieuKhungCuon(scroll, 500, 160, 300, 120);
        scroll.getVerticalScrollBar().setUnitIncrement(30);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); // ẩn thanh cuộn, vẫn lăn chuột được

        the.noiDung.add(scroll, BorderLayout.CENTER);
        return the;
    }

    // Thành phần form dùng chung
    private void themDong(JPanel pnl, int hang, String nhan, JComponent o) {
        themDong(pnl, hang, nhan, o, 122);
    }

    private void themDong(JPanel pnl, int hang, String nhan, JComponent o, int rongNhan) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = hang;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 0, 4, 10);
        pnl.add(taoNhan(nhan, rongNhan), g);

        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);
        pnl.add(o, g);
    }

    private void themDongKep(JPanel pnl, int hang, String nhan1, JComponent o1, double w1,
            String nhan2, JComponent o2, double w2) {
        o1.setPreferredSize(new Dimension(10, 32));
        o2.setPreferredSize(new Dimension(10, 32));

        JPanel dong = new JPanel(new GridBagLayout());
        dong.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0;
        g.insets = new Insets(0, 0, 0, 10);
        dong.add(taoNhan(nhan1, 78), g);

        g.gridx = 1;
        g.weightx = w1;
        g.insets = new Insets(0, 0, 0, 14);
        dong.add(o1, g);

        g.gridx = 2;
        g.weightx = 0;
        g.insets = new Insets(0, 0, 0, 8);
        dong.add(taoNhan(nhan2, 78), g);

        g.gridx = 3;
        g.weightx = w2;
        g.insets = new Insets(0, 0, 0, 0);
        dong.add(o2, g);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = hang;
        gc.gridwidth = 2;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 0, 4, 0);
        pnl.add(dong, gc);
    }

    private JLabel taoNhan(String nhan, int rong) {
        JLabel lbl = new JLabel(nhan);
        lbl.setFont(FONT_THUONG);
        lbl.setForeground(MAU_PHU);
        lbl.setPreferredSize(new Dimension(rong, 32));
        return lbl;
    }

    private void themTieuMuc(JPanel pnl, int hang, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_NHO_DAM);
        lbl.setForeground(MAU_NUT_DAM);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MAU_VIEN),
                new EmptyBorder(0, 0, 4, 0)));

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = hang;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(12, 0, 4, 0);
        pnl.add(lbl, g);
    }

    private void themDongTien(JPanel pnl, int hang, String nhan, JLabel giaTri) {
        JLabel lbl = new JLabel(nhan);
        lbl.setFont(FONT_THUONG);
        lbl.setForeground(MAU_PHU);

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = hang;
        g.insets = new Insets(5, 12, 5, 0);
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        pnl.add(lbl, g);

        g.gridx = 1;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.EAST;
        pnl.add(giaTri, g);
    }

    // KIỂU DÁNG
    private void kieuCacOComponent() {
        for (JTextField t : new JTextField[] {
                txtMaHoaDon, txtNgayLap, txtMaNhanVienLapHoaDon, txtTenNhanVienLapHoaDon }) {
            kieuO(t, true);
        }
        for (JTextField t : oTuDien) {
            kieuO(t, true);
        }
        kieuO(txtMaPhieuSuaChua, false);
        kieuO(txtDonGia, false);
        txtMaPhieuSuaChua.setToolTipText("Nhập mã phiếu sửa chữa rồi nhấn Enter");

        for (JComboBox<?> c : new JComboBox<?>[] { cboLoaiHoaDon, cboPhuTung, cboKhuyenMai }) {
            c.setFont(FONT_THUONG);
            c.setBackground(Color.WHITE);
            c.setPreferredSize(new Dimension(120, 32));
        }
        cboKhuyenMai.setMaximumRowCount(8);
        cboKhuyenMai.setToolTipText("Chọn chương trình khuyến mãi");

        spnSoLuong.setFont(FONT_THUONG);
        spnSoLuong.setPreferredSize(new Dimension(120, 32));

        lblSoLichSu.setFont(FONT_THUONG);
        lblSoLichSu.setForeground(MAU_PHU);

        // Các nhãn số tiền ở khu tổng kết
        for (JLabel lbl : new JLabel[] {
                lblTienDichVu, lblTienPhuTung, lblTongHoaDon, lblTienVAT, lblTienGiam }) {
            lbl.setFont(FONT_GIA_TRI);
            lbl.setForeground(MAU_CHU);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            lbl.setPreferredSize(new Dimension(160, 28));
        }
        lblTienGiam.setForeground(MAU_DO);
        lblTongHoaDon.setForeground(MAU_NUT);

        lblTongThanhToan.setFont(FONT_TONG);
        lblTongThanhToan.setForeground(MAU_XANH_LA);
        lblTongThanhToan.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTongThanhToan.setPreferredSize(new Dimension(200, 34));
    }

    private void kieuO(JTextField txt, boolean chiDoc) {
        txt.setFont(FONT_THUONG);
        txt.setForeground(MAU_CHU);
        txt.setEditable(!chiDoc);
        txt.setBackground(chiDoc ? MAU_NEN_O : Color.WHITE);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MAU_VIEN),
                new EmptyBorder(4, 8, 4, 8)));
        txt.setPreferredSize(new Dimension(120, 32));
    }

    private void kieuKhungCuon(JScrollPane sp, int rong, int cao, int rongMin, int caoMin) {
        sp.setBorder(BorderFactory.createLineBorder(MAU_VIEN));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setPreferredSize(new Dimension(rong, cao));
        sp.setMinimumSize(new Dimension(rongMin, caoMin));
    }

    // Kiểu chung cho cả ba bảng: font, dòng kẻ, màu chọn, tiêu đề cột, bề rộng cột.
    private void kieuBang(JTable t, int caoDong, int... rong) {
        t.setFont(FONT_THUONG);
        t.setRowHeight(caoDong);
        t.setShowVerticalLines(false);
        t.setGridColor(MAU_VIEN);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setSelectionBackground(MAU_CHON);
        t.setSelectionForeground(MAU_CHU);

        JTableHeader header = t.getTableHeader();
        header.setReorderingAllowed(false);
        header.setFont(FONT_DAM);
        header.setBackground(MAU_NEN_TIEU_DE);
        header.setForeground(MAU_CHU);
        header.setPreferredSize(new Dimension(0, 34));

        for (int i = 0; i < rong.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
        }
    }

    private void datRenderer(JTable t, TableCellRenderer... renderer) {
        for (int i = 0; i < renderer.length; i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(renderer[i]);
        }
    }

    private void cauHinhBangChiTiet() {
        kieuBang(tblChiTiet, 30, 80, 90, 280, 70, 120, 130);
        TableCellRenderer giua = rendererChu(SwingConstants.CENTER, 0, 0);
        TableCellRenderer trai = rendererChu(SwingConstants.LEFT, 10, 6);
        TableCellRenderer tien = rendererTien(SwingConstants.RIGHT, 6, 12);
        datRenderer(tblChiTiet, giua, giua, trai, giua, tien, tien);
    }

    private void cauHinhBangBaoHanh() {
        kieuBang(tblBaoHanh, 28, 190, 75, 190, 190);
    }

    private void cauHinhBangLichSu() {
        kieuBang(tblLichSu, 30, 130, 110, 100, 170, 160, 140);
        TableCellRenderer giua = rendererChu(SwingConstants.CENTER, 0, 0);
        TableCellRenderer tien = rendererTien(SwingConstants.CENTER, 6, 6);
        datRenderer(tblLichSu, giua, giua, giua, giua, tien, giua);

        modelLichSu.addTableModelListener(
                e -> lblSoLichSu.setText(modelLichSu.getRowCount() + " hóa đơn"));
    }

    private static DefaultTableCellRenderer rendererChu(int canh, int trai, int phai) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(canh);
        r.setBorder(new EmptyBorder(0, trai, 0, phai));
        return r;
    }

    private static DefaultTableCellRenderer rendererTien(int canh, int trai, int phai) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object v) {
                setText(v instanceof BigDecimal b ? dinhDangTien(b) : "");
            }
        };
        r.setHorizontalAlignment(canh);
        r.setBorder(new EmptyBorder(0, trai, 0, phai));
        return r;
    }

    // SỰ KIỆN
    private void caiDatSuKien() {
        cboLoaiHoaDon.addActionListener(e -> capNhatLoaiHoaDon());
        btnThemHoaDon.addActionListener(e -> lamMoiToanBo());
        btnTimPhieu.addActionListener(e -> hienThiCuaSoTimPhieu());

        // Nhập mã phiếu rồi nhấn Enter để tải thông tin
        txtMaPhieuSuaChua.addActionListener(e -> {
            String ma = txtMaPhieuSuaChua.getText().trim();
            if (!ma.isEmpty()) {
                napThongTinPhieu(ma);
            }
        });

        txtNgayLap.getDocument().addDocumentListener(khiDoi(this::capNhatThoiHanBaoHanhTrongBang));
        cboPhuTung.addActionListener(e -> capNhatDonGiaPhuTung());

        // Bảng chi tiết hoặc khuyến mãi đổi thì tính lại tổng (khi đang nạp hóa đơn cũ
        // thì giữ nguyên số liệu đã lưu)
        modelChiTiet.addTableModelListener(e -> tinhTongNeuDangLap());
        cboKhuyenMai.addActionListener(e -> tinhTongNeuDangLap());

        // Chọn một hóa đơn trong lịch sử để xem lại
        tblLichSu.getSelectionModel().addListSelectionListener(e -> {
            int row = tblLichSu.getSelectedRow();
            if (e.getValueIsAdjusting() || dangDongBoLichSu || row < 0) {
                return;
            }
            String maHD = modelLichSu.getValueAt(tblLichSu.convertRowIndexToModel(row), 0).toString();
            napHoaDonTuLichSu(maHD);
        });
    }

    private void tinhTongNeuDangLap() {
        if (!dangNapHoaDonCu) {
            tinhTongHoaDon();
        }
    }

    private static DocumentListener khiDoi(Runnable viec) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                viec.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                viec.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                viec.run();
            }
        };
    }

    private void capNhatLoaiHoaDon() {
        String loai = (String) cboLoaiHoaDon.getSelectedItem();

        if (SUA_CHUA.equals(loai)) {
            cardNguon.show(pnlNguon, THE_SUA_CHUA);
            theNguon.datTieuDe("PHIẾU SỬA CHỮA");
        } else if (MUA_PHU_TUNG.equals(loai)) {
            cardNguon.show(pnlNguon, THE_PHU_TUNG);
            theNguon.datTieuDe("CHỌN PHỤ TÙNG");
        } else {
            cardNguon.show(pnlNguon, THE_TRONG);
            theNguon.datTieuDe("NGUỒN HÓA ĐƠN");
        }

        // Đang nạp hóa đơn cũ: chỉ đổi giao diện nguồn, không xóa dữ liệu vừa tải
        if (!dangNapHoaDonCu) {
            maHoaDonVuaLuu = null;
            xoaKhuyenMaiNgungApDung();
            for (JTextField t : oTuDien) {
                t.setText("");
            }
            modelChiTiet.setRowCount(0);
            xoaBaoHanh(MSG_CHON_PHIEU);
            lamMoiNhapLieu();
            tinhTongHoaDon();
        }

        pnlNguon.revalidate();
        revalidate();
        repaint();
    }

    private void capNhatDonGiaPhuTung() {
        PhuTung pt = (PhuTung) cboPhuTung.getSelectedItem();
        txtDonGia.setText(pt == null ? "0" : pt.giaBan().toPlainString());
    }

    // TẢI DỮ LIỆU
    private <T> void chayNen(Callable<T> viec, Consumer<T> khiXong, String tieuDeLoi) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return viec.call();
            }

            @Override
            protected void done() {
                try {
                    khiXong.accept(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    baoLoi(tieuDeLoi, ex);
                } catch (ExecutionException ex) {
                    Throwable goc = ex.getCause() == null ? ex : ex.getCause();
                    baoLoi(tieuDeLoi, goc instanceof Exception e ? e : new Exception(goc));
                }
            }
        }.execute();
    }

    private void capNhatMaHoaDon() {
        try {
            String ma = hoaDonDAO.taoMaHoaDonMoi();
            if (!ma.isEmpty()) {
                txtMaHoaDon.setText(ma);
            }
        } catch (SQLException ex) {
            baoLoi("Không thể phát sinh mã hóa đơn", ex);
        }
    }

    // Điền thông tin khách, xe và chi tiết từ phiếu sửa chữa.
    private boolean napThongTinPhieu(String maPhieu) {
        try {
            Phieu p = hoaDonDAO.layPhieu(maPhieu);
            if (p == null) {
                canhBao("Không tìm thấy phiếu sửa chữa: " + maPhieu);
                return false;
            }
            txtMaPhieuSuaChua.setText(p.ma());
            txtMaKhachHang.setText(p.maKH());
            txtTenKhachHang.setText(p.tenKH());
            txtSoDienThoai.setText(p.soDT());
            txtBienSoXe.setText(p.bienSo());
            txtTenXe.setText(p.tenXe());

            List<DongChiTiet> ds = hoaDonDAO.layChiTietPhieu(maPhieu);
            modelChiTiet.setRowCount(0);
            ds.forEach(this::themDongChiTiet);
            hienThiBaoHanh(chinhSachBaoHanhDAO.layPhuTungBaoHanhTheoPhieu(maPhieu), MSG_KHONG_BAO_HANH);

            if (ds.isEmpty()) {
                canhBao("Phiếu sửa chữa chưa có dịch vụ hoặc phụ tùng.");
            }
            return true;
        } catch (SQLException ex) {
            baoLoi("Không thể tải thông tin phiếu hoặc kiểm tra bảo hành", ex);
            return false;
        }
    }

    // Xem lại hóa đơn từ lịch sử
    private void napHoaDonTuLichSu(String maHD) {
        chayNen(() -> {
            ThongTinHoaDon hd = hoaDonDAO.layThongTinHoaDon(maHD);
            if (hd == null) {
                throw new SQLException("Không tìm thấy hóa đơn " + maHD);
            }
            List<DongChiTiet> dsChiTiet = hoaDonDAO.layChiTietHoaDon(maHD);
            List<PhuTungBaoHanh> dsBaoHanh = coPhieu(hd.maPhieu())
                    ? chinhSachBaoHanhDAO.layPhuTungBaoHanhTheoPhieu(hd.maPhieu())
                    : List.of();
            return new HoaDonDaLuu(hd, dsChiTiet, dsBaoHanh);
        }, this::hienThiHoaDonCu, "Không thể tải thông tin hóa đơn");
    }

    private void hienThiHoaDonCu(HoaDonDaLuu cu) {
        ThongTinHoaDon hd = cu.hd();
        boolean laSuaChua = coPhieu(hd.maPhieu());

        dangNapHoaDonCu = true;
        try {
            dangXemHoaDonCu = true;
            maHoaDonVuaLuu = null; // hóa đơn cũ không dùng để thanh toán lại từ màn này

            // Thông tin hóa đơn
            txtMaHoaDon.setText(hd.maHoaDon());
            txtNgayLap.setText(hd.ngayLap() == null ? "" : hd.ngayLap().format(DINH_DANG_NGAY));
            txtMaNhanVienLapHoaDon.setText(nz(hd.maNV()));
            txtTenNhanVienLapHoaDon.setText(nz(hd.tenNV()));
            cboLoaiHoaDon.setSelectedItem(laSuaChua ? SUA_CHUA : MUA_PHU_TUNG);

            // Phiếu sửa chữa, khách hàng, xe
            txtMaPhieuSuaChua.setText(nz(hd.maPhieu()));
            txtMaKhachHang.setText(nz(hd.maKH()));
            txtTenKhachHang.setText(nz(hd.tenKH()));
            txtSoDienThoai.setText(nz(hd.soDT()));
            txtBienSoXe.setText(nz(hd.bienSo()));
            txtTenXe.setText(nz(hd.tenXe()));

            // Chi tiết, khuyến mãi, tổng kết (theo số liệu đã lưu)
            modelChiTiet.setRowCount(0);
            cu.dsChiTiet().forEach(this::themDongChiTiet);
            cboKhuyenMai.setSelectedItem(timKhuyenMai(hd));
            hienThiTongKetHoaDonCu(hd, cu.dsChiTiet());

            // Bảo hành
            hienThiBaoHanh(cu.dsBaoHanh(), laSuaChua ? MSG_KHONG_BAO_HANH : MSG_HD_MUA_PHU_TUNG);

            revalidate();
            repaint();
        } finally {
            dangNapHoaDonCu = false;
        }
    }

    // Tìm khuyến mãi đã áp dụng; nếu đã hết hạn thì thêm tạm vào combo để hiển thị.
    private KhuyenMai timKhuyenMai(ThongTinHoaDon hd) {
        if (hd.maKM() == null || hd.maKM().isBlank()) {
            return cboKhuyenMai.getItemCount() > 0 ? cboKhuyenMai.getItemAt(0) : null;
        }
        for (int i = 0; i < cboKhuyenMai.getItemCount(); i++) {
            KhuyenMai km = cboKhuyenMai.getItemAt(i);
            if (km.ma().equals(hd.maKM())) {
                return km;
            }
        }
        xoaKhuyenMaiNgungApDung();
        khuyenMaiNgungApDung = new KhuyenMai(hd.maKM(),
                hd.tenKM() == null ? "Khuyến mãi đã ngừng áp dụng" : hd.tenKM(), hd.tileGiam());
        cboKhuyenMai.addItem(khuyenMaiNgungApDung);
        return khuyenMaiNgungApDung;
    }

    private void xoaKhuyenMaiNgungApDung() {
        if (khuyenMaiNgungApDung != null) {
            cboKhuyenMai.removeItem(khuyenMaiNgungApDung);
            khuyenMaiNgungApDung = null;
        }
    }

    private void hienThiTongKetHoaDonCu(ThongTinHoaDon hd, List<DongChiTiet> dsChiTiet) {
        BigDecimal dichVu = BigDecimal.ZERO;
        BigDecimal phuTung = BigDecimal.ZERO;
        for (DongChiTiet d : dsChiTiet) {
            if (d.dichVu()) {
                dichVu = dichVu.add(d.thanhTien());
            } else {
                phuTung = phuTung.add(d.thanhTien());
            }
        }

        tienVAT = hd.tienVAT();
        tienGiam = hd.tienGiam();
        tongThanhToan = hd.soTien();
        hienThiTong(dichVu, phuTung, dichVu.add(phuTung).add(tienVAT));
    }

    // Bảo hành
    private void hienThiBaoHanh(List<PhuTungBaoHanh> ds, String thongBaoRong) {
        modelBaoHanh.setRowCount(0);
        dsBaoHanhHienThi.clear();
        dsBaoHanhHienThi.addAll(ds);
        for (PhuTungBaoHanh b : ds) {
            modelBaoHanh.addRow(new Object[] {
                    b.tenPhuTung(), b.soLuong(), b.tenChinhSach(), hienThiThoiHanBaoHanh(b) });
        }
        lblTinhTrangBaoHanh.setText(ds.isEmpty()
                ? thongBaoRong
                : ds.size() + " phụ tùng được áp dụng bảo hành");
    }

    private void xoaBaoHanh(String thongBao) {
        hienThiBaoHanh(List.of(), thongBao);
    }

    private void capNhatThoiHanBaoHanhTrongBang() {
        for (int row = 0; row < dsBaoHanhHienThi.size(); row++) {
            modelBaoHanh.setValueAt(hienThiThoiHanBaoHanh(dsBaoHanhHienThi.get(row)), row, 3);
        }
    }

    private String hienThiThoiHanBaoHanh(PhuTungBaoHanh b) {
        String donVi = switch (b.donViThoiGian()) {
            case "NGAY" -> "ngày";
            case "NAM" -> "năm";
            default -> "tháng";
        };
        String hienThi = b.thoiHan() + " " + donVi;
        try {
            LocalDate ngayLap = LocalDate.parse(txtNgayLap.getText().trim(), DINH_DANG_NGAY);
            LocalDate ngayHetHan = switch (b.donViThoiGian()) {
                case "NGAY" -> ngayLap.plusDays(b.thoiHan());
                case "NAM" -> ngayLap.plusYears(b.thoiHan());
                default -> ngayLap.plusMonths(b.thoiHan());
            };
            return hienThi + " (đến " + ngayHetHan.format(DINH_DANG_NGAY) + ")";
        } catch (DateTimeParseException ex) {
            return hienThi;
        }
    }

    private String taoThongTinBaoHanhTooltip(PhuTungBaoHanh b) {
        StringBuilder sb = new StringBuilder("<html><b>").append(thoatHtml(b.tenChinhSach())).append("</b>");
        themDongTooltip(sb, "Nội dung", b.noiDungBaoHanh());
        themDongTooltip(sb, "Điều kiện", b.dieuKienBaoHanh());
        themDongTooltip(sb, "Từ chối", b.truongHopTuChoi());
        return sb.append("</html>").toString();
    }

    private void themDongTooltip(StringBuilder sb, String nhan, String noiDung) {
        if (noiDung != null && !noiDung.isBlank()) {
            sb.append("<br>").append(nhan).append(": ").append(thoatHtml(noiDung));
        }
    }

    private String thoatHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // NGHIỆP VỤ
    private void themDongChiTiet(DongChiTiet d) {
        modelChiTiet.addRow(new Object[] {
                d.dichVu() ? DICH_VU : PHU_TUNG,
                d.ma(), d.ten(), d.soLuong(), d.donGia(), d.thanhTien() });
    }

    private List<DongChiTiet> layChiTietTuBang() {
        List<DongChiTiet> ds = new ArrayList<>();
        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            ds.add(new DongChiTiet(
                    DICH_VU.equals(modelChiTiet.getValueAt(i, 0)),
                    (String) modelChiTiet.getValueAt(i, 1),
                    (String) modelChiTiet.getValueAt(i, 2),
                    (Integer) modelChiTiet.getValueAt(i, 3),
                    (BigDecimal) modelChiTiet.getValueAt(i, 4)));
        }
        return ds;
    }

    private void themPhuTung() {
        PhuTung pt = (PhuTung) cboPhuTung.getSelectedItem();
        if (pt == null || pt.ma().isEmpty()) {
            canhBao("Vui lòng chọn phụ tùng.");
            return;
        }

        BigDecimal donGia;
        try {
            donGia = new BigDecimal(txtDonGia.getText().trim());
        } catch (NumberFormatException ex) {
            donGia = null;
        }
        if (donGia == null || donGia.signum() < 0) {
            canhBao("Đơn giá phải là số hợp lệ và không âm.");
            return;
        }

        int soLuongThem = (Integer) spnSoLuong.getValue();
        int row = timDongPhuTung(pt.ma());

        if (row >= 0) {
            // Đã có: cộng số lượng, giữ đơn giá cũ của dòng
            int soLuongMoi = (Integer) modelChiTiet.getValueAt(row, 3) + soLuongThem;
            BigDecimal donGiaCu = (BigDecimal) modelChiTiet.getValueAt(row, 4);
            modelChiTiet.setValueAt(soLuongMoi, row, 3);
            modelChiTiet.setValueAt(donGiaCu.multiply(BigDecimal.valueOf(soLuongMoi)), row, 5);
        } else {
            themDongChiTiet(new DongChiTiet(false, pt.ma(), pt.ten(), soLuongThem, donGia));
        }
        lamMoiNhapLieu();
    }

    // Vị trí dòng phụ tùng có mã đã cho trong bảng chi tiết, -1 nếu chưa có. */
    private int timDongPhuTung(String maPhuTung) {
        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            if (PHU_TUNG.equals(modelChiTiet.getValueAt(i, 0))
                    && maPhuTung.equals(modelChiTiet.getValueAt(i, 1))) {
                return i;
            }
        }
        return -1;
    }

    private void xoaDongChiTiet() {
        int row = tblChiTiet.getSelectedRow();
        if (row < 0) {
            canhBao("Vui lòng chọn dòng cần xóa.");
            return;
        }
        modelChiTiet.removeRow(tblChiTiet.convertRowIndexToModel(row));
    }

    private void tinhTongHoaDon() {
        BigDecimal dichVu = BigDecimal.ZERO;
        BigDecimal phuTung = BigDecimal.ZERO;
        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            BigDecimal thanhTien = (BigDecimal) modelChiTiet.getValueAt(i, 5);
            if (DICH_VU.equals(modelChiTiet.getValueAt(i, 0))) {
                dichVu = dichVu.add(thanhTien);
            } else {
                phuTung = phuTung.add(thanhTien);
            }
        }

        BigDecimal truocThue = dichVu.add(phuTung);
        tienVAT = truocThue.multiply(VAT).setScale(0, RoundingMode.HALF_UP);
        BigDecimal tamTinh = truocThue.add(tienVAT);

        KhuyenMai km = (KhuyenMai) cboKhuyenMai.getSelectedItem();
        BigDecimal tile = km == null ? BigDecimal.ZERO : km.tile();
        tienGiam = tamTinh.multiply(tile)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .min(tamTinh);
        tongThanhToan = tamTinh.subtract(tienGiam);

        hienThiTong(dichVu, phuTung, tamTinh);
    }

    // Đổ các số đã tính vào nhãn (tienVAT, tienGiam, tongThanhToan lấy từ trường).
    private void hienThiTong(BigDecimal dichVu, BigDecimal phuTung, BigDecimal tongHoaDon) {
        lblTienDichVu.setText(dinhDangTien(dichVu));
        lblTienPhuTung.setText(dinhDangTien(phuTung));
        lblTienVAT.setText(dinhDangTien(tienVAT));
        lblTongHoaDon.setText(dinhDangTien(tongHoaDon));
        lblTienGiam.setText(dinhDangTien(tienGiam));
        lblTongThanhToan.setText(dinhDangTien(tongThanhToan));
    }

    private void luuHoaDon() {
        if (dangXemHoaDonCu) {
            canhBao("Đây là hóa đơn đã lưu. Nhấn \"Thêm\" để lập hóa đơn mới.");
            return;
        }

        String loai = (String) cboLoaiHoaDon.getSelectedItem();
        boolean laSuaChua = SUA_CHUA.equals(loai);
        if (!laSuaChua && !MUA_PHU_TUNG.equals(loai)) {
            canhBao("Vui lòng chọn loại hóa đơn.");
            return;
        }

        String maHD = txtMaHoaDon.getText().trim();
        if (maHD.isEmpty() || maHD.equalsIgnoreCase("Tự động")) {
            canhBao("Mã hóa đơn chưa được tạo.");
            capNhatMaHoaDon();
            return;
        }

        // Người lập hóa đơn lấy từ tài khoản đăng nhập
        String maNV = txtMaNhanVienLapHoaDon.getText().trim();
        if (maNV.isEmpty()) {
            canhBao("Chưa có thông tin nhân viên đang đăng nhập. "
                    + "Vui lòng đăng nhập lại hoặc kiểm tra cấu hình tài khoản.");
            return;
        }

        String maPhieu = laSuaChua ? txtMaPhieuSuaChua.getText().trim() : null;
        if (laSuaChua && maPhieu.isEmpty()) {
            canhBao("Vui lòng nhập mã phiếu sửa chữa.");
            return;
        }
        if (modelChiTiet.getRowCount() == 0) {
            canhBao("Vui lòng thêm chi tiết hóa đơn.");
            return;
        }

        LocalDate ngayLap;
        try {
            ngayLap = LocalDate.parse(txtNgayLap.getText().trim(), DINH_DANG_NGAY);
        } catch (DateTimeParseException ex) {
            canhBao("Ngày lập hóa đơn không hợp lệ.");
            return;
        }

        KhuyenMai km = (KhuyenMai) cboKhuyenMai.getSelectedItem();
        String maKM = (km == null || km.ma().isEmpty()) ? null : km.ma();

        try {
            hoaDonDAO.luuHoaDon(maHD, maPhieu, maNV, maKM, loai, ngayLap,
                    tienGiam, tienVAT, tongThanhToan, layChiTietTuBang());
            maHoaDonVuaLuu = maHD;
            themVaoDauLichSu(new LichSuHoaDon(maHD, ngayLap, maNV, maPhieu, tongThanhToan, CHUA_THANH_TOAN));
            JOptionPane.showMessageDialog(this,
                    "Lưu hóa đơn thành công!\nMã hóa đơn: " + maHD,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            baoLoi("Không thể lưu hóa đơn", ex);
        }
    }

    private void moFormThanhToan() {
        if (maHoaDonVuaLuu == null) {
            canhBao("Vui lòng lưu hóa đơn trước khi thanh toán!");
            return;
        }
        FrmThanhToan frm = new FrmThanhToan(maHoaDonVuaLuu);
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }

    // Xóa phần nhập phiếu/phụ tùng, giữ nguyên bảng chi tiết.
    private void lamMoiNhapLieu() {
        txtMaPhieuSuaChua.setText("");
        if (cboPhuTung.getItemCount() > 0) {
            cboPhuTung.setSelectedIndex(0);
        } else {
            txtDonGia.setText("0");
        }
        spnSoLuong.setValue(1);
    }

    // Đưa form về trạng thái lập hóa đơn mới.
    private void lamMoiToanBo() {
        dangXemHoaDonCu = false;
        if (cboKhuyenMai.getItemCount() > 0) {
            cboKhuyenMai.setSelectedIndex(0);
        }
        txtNgayLap.setText(LocalDate.now().format(DINH_DANG_NGAY));
        cboLoaiHoaDon.setSelectedIndex(0);
        capNhatLoaiHoaDon(); // xóa phiếu, bảng chi tiết, ô phụ tùng

        dangDongBoLichSu = true;
        tblLichSu.clearSelection();
        dangDongBoLichSu = false;

        capNhatMaHoaDon();
    }

    // Lịch sử
    private Object[] dongLichSu(LichSuHoaDon h) {
        return new Object[] {
                h.ma(),
                h.ngayLap() == null ? "" : h.ngayLap().format(DINH_DANG_NGAY),
                h.maNV(),
                coPhieu(h.maPhieu()) ? h.maPhieu() : "—",
                h.tongThanhToan(),
                h.trangThai() };
    }

    // Đưa hóa đơn lên đầu lịch sử (thay dòng cũ nếu trùng mã) và chọn dòng đó.
    private void themVaoDauLichSu(LichSuHoaDon h) {
        dangDongBoLichSu = true; // chọn bằng code, không tải lại hóa đơn
        try {
            for (int i = modelLichSu.getRowCount() - 1; i >= 0; i--) {
                if (h.ma().equals(modelLichSu.getValueAt(i, 0))) {
                    modelLichSu.removeRow(i);
                }
            }
            modelLichSu.insertRow(0, dongLichSu(h));
            tblLichSu.setRowSelectionInterval(0, 0);
            tblLichSu.scrollRectToVisible(tblLichSu.getCellRect(0, 0, true));
        } finally {
            dangDongBoLichSu = false;
        }
    }

    // Cửa sổ tìm phiếu sửa chữa
    private void hienThiCuaSoTimPhieu() {
        JFrame win = new JFrame("Tìm phiếu sửa chữa");
        win.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        win.setSize(1000, 480);
        win.setMinimumSize(new Dimension(800, 400));
        win.setLocationRelativeTo(this);
        win.setContentPane(new FrmTimPhieuSuaChua(maPhieu -> {
            if (napThongTinPhieu(maPhieu)) {
                win.dispose();
            }
        }));
        win.setVisible(true);
    }

    // TIỆN ÍCH
    private static String dinhDangTien(BigDecimal tien) {
        return DINH_DANG_TIEN.format(tien) + " đ";
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static boolean coPhieu(String maPhieu) {
        return maPhieu != null && !maPhieu.isBlank();
    }

    private void canhBao(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void baoLoi(String tieuDe, Exception ex) {
        JOptionPane.showMessageDialog(this, tieuDe + ":\n" + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // Nút chính: nền xanh, chữ trắng.
    private JButton taoNut(String text) {
        return taoNutMau(text, MAU_NUT, MAU_NUT_DAM, Color.WHITE, MAU_NUT_DAM);
    }

    // Nút phụ: nền trắng, viền xám, chữ tối.
    private JButton taoNutPhu(String text) {
        return taoNutMau(text, Color.WHITE, new Color(241, 245, 249), MAU_CHU, new Color(180, 190, 205));
    }

    private JButton taoNutMau(String text, Color nen, Color nenKhiRe, Color chu, Color vien) {
        JButton btn = new JButton(text);

        // BasicButtonUI để Look and Feel không ghi đè màu nền
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setFont(FONT_DAM);
        btn.setForeground(chu);
        btn.setBackground(nen);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(vien, 1),
                BorderFactory.createEmptyBorder(4, 14, 4, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Bề rộng theo chữ, tối thiểu 90px, cao đều 34px
        int rong = Math.max(90, btn.getFontMetrics(FONT_DAM).stringWidth(text) + 40);
        btn.setPreferredSize(new Dimension(rong, 34));
        btn.setMinimumSize(new Dimension(rong, 34));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(nenKhiRe);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(nen);
            }
        });
        return btn;
    }

    // LỚP LỒNG HỖ TRỢ GIAO DIỆN
    private static class MoHinhBang extends DefaultTableModel {
        MoHinhBang(String... tenCot) {
            super(tenCot, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private static class BangSoc extends JTable {
        BangSoc(TableModel model) {
            super(model);
        }

        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (!isRowSelected(row)) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : MAU_DONG_CHAN);
                c.setForeground(MAU_CHU);
            }
            return c;
        }
    }

    // Thẻ (card) có thanh tiêu đề, dùng cho mọi khu vực của màn hình.
    private static class The extends JPanel {
        final JPanel noiDung = new JPanel(new GridBagLayout());
        private final JLabel lblTieuDe = new JLabel();
        private final JPanel pnlDau = new JPanel(new BorderLayout(8, 0));

        The(String tieuDe) {
            super(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(MAU_VIEN));

            lblTieuDe.setFont(FONT_DAM);
            lblTieuDe.setForeground(MAU_NUT_DAM);
            datTieuDe(tieuDe);

            pnlDau.setBackground(MAU_NEN_TIEU_DE);
            pnlDau.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, MAU_VIEN),
                    new EmptyBorder(8, 12, 8, 12)));
            pnlDau.add(lblTieuDe, BorderLayout.WEST);

            noiDung.setBackground(Color.WHITE);
            noiDung.setBorder(new EmptyBorder(10, 12, 12, 12));

            add(pnlDau, BorderLayout.NORTH);
            add(noiDung, BorderLayout.CENTER);
        }

        void datTieuDe(String tieuDe) {
            lblTieuDe.setText(tieuDe);
        }

        void themBenPhaiTieuDe(JComponent c) {
            pnlDau.add(c, BorderLayout.EAST);
        }
    }

    private static class CardTheoThe extends CardLayout {
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets in = parent.getInsets();
                for (Component c : parent.getComponents()) {
                    if (c.isVisible()) {
                        Dimension d = c.getPreferredSize();
                        return new Dimension(d.width + in.left + in.right,
                                d.height + in.top + in.bottom);
                    }
                }
                return super.preferredLayoutSize(parent);
            }
        }
    }

    // Panel cuộn: co giãn theo chiều ngang, tự giãn đầy khung khi cửa sổ cao.
    private static class TrangCuon extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return 120;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            Container cha = getParent();
            return cha instanceof JViewport && cha.getHeight() > getPreferredSize().height;
        }
    }

    private static class CotNoiDung extends JPanel {
        private final int rongUaThich;
        private final int rongToiThieu;

        CotNoiDung(int rongUaThich, int rongToiThieu) {
            super(new GridBagLayout());
            setOpaque(false);
            this.rongUaThich = rongUaThich;
            this.rongToiThieu = rongToiThieu;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(d.width, rongUaThich), d.height);
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            return new Dimension(Math.max(d.width, rongToiThieu), d.height);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BaseFrame frame = new BaseFrame();
            FrmHoaDon frmHoaDon = new FrmHoaDon();
            frame.setContent(frmHoaDon);
            frmHoaDon.taiDuLieuBanDauAsync();
            frame.setVisible(true);
        });
    }
}