package dao;

import connectDB.ConnectDB;
import entity.DichVu;
import util.MaTuDong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {
    private static final String CAU_LENH_LAY_DANH_SACH = """
            SELECT maDV, tenDV, moTaDV, giaDV
            FROM DichVu
            ORDER BY maDV
            """;

    public List<DichVu> findAll() throws SQLException {
        List<DichVu> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(CAU_LENH_LAY_DANH_SACH);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(anhXa(resultSet));
            }
        }
        return danhSach;
    }

    public DichVu findById(String maDV) throws SQLException {
        kiemTraMa(maDV);
        String sql = "SELECT maDV, tenDV, moTaDV, giaDV FROM DichVu WHERE maDV = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maDV.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? anhXa(resultSet) : null;
            }
        }
    }

    public String layMaTiepTheo() throws SQLException {
        List<String> danhSachMa = new ArrayList<>();
        for (DichVu dichVu : findAll()) {
            danhSachMa.add(dichVu.getMaDV());
        }
        return MaTuDong.sinhMa(MaTuDong.TIEN_TO_DICH_VU, danhSachMa, ma -> ma);
    }

    public boolean them(DichVu dichVu) throws SQLException {
        kiemTraDuLieu(dichVu);
        String sql = "INSERT INTO DichVu (maDV, tenDV, moTaDV, giaDV) VALUES (?, ?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ganThamSo(statement, dichVu);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean capNhat(DichVu dichVu) throws SQLException {
        kiemTraDuLieu(dichVu);
        String sql = "UPDATE DichVu SET tenDV = ?, moTaDV = ?, giaDV = ? WHERE maDV = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, dichVu.getTenDV().trim());
            statement.setString(2, dichVu.getMoTaDV());
            ganGiaTien(statement, 3, dichVu.getGiaDV());
            statement.setString(4, dichVu.getMaDV().trim());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean xoa(String maDV) throws SQLException {
        kiemTraMa(maDV);
        String sql = "DELETE FROM DichVu WHERE maDV = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maDV.trim());
            return statement.executeUpdate() > 0;
        }
    }

    // Giữ API CRUD cũ cho các form chưa chuyển hoàn toàn sang controller.
    public boolean insert(DichVu dichVu) throws SQLException {
        return them(dichVu);
    }

    public boolean update(DichVu dichVu) throws SQLException {
        return capNhat(dichVu);
    }

    public boolean deleteById(String maDV) throws SQLException {
        return xoa(maDV);
    }

    private Connection moKetNoi() throws SQLException {
        ConnectDB.getInstance().connect();
        Connection connection = ConnectDB.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu.");
        }
        return connection;
    }

    private DichVu anhXa(ResultSet resultSet) throws SQLException {
        DichVu dichVu = new DichVu();
        dichVu.setMaDV(resultSet.getString("maDV"));
        dichVu.setTenDV(resultSet.getString("tenDV"));
        dichVu.setMoTaDV(resultSet.getString("moTaDV"));
        java.math.BigDecimal giaDV = resultSet.getBigDecimal("giaDV");
        dichVu.setGiaDV(giaDV == null ? null : giaDV.doubleValue());
        return dichVu;
    }

    private void ganThamSo(PreparedStatement statement, DichVu dichVu) throws SQLException {
        statement.setString(1, dichVu.getMaDV().trim());
        statement.setString(2, dichVu.getTenDV().trim());
        statement.setString(3, dichVu.getMoTaDV());
        ganGiaTien(statement, 4, dichVu.getGiaDV());
    }

    private void ganGiaTien(PreparedStatement statement, int viTri, Double giaDV) throws SQLException {
        if (giaDV == null) {
            statement.setNull(viTri, java.sql.Types.DECIMAL);
        } else {
            statement.setDouble(viTri, giaDV);
        }
    }

    private void kiemTraDuLieu(DichVu dichVu) throws SQLException {
        if (dichVu == null) {
            throw new SQLException("Thông tin dịch vụ không được để trống.");
        }
        kiemTraMa(dichVu.getMaDV());
        if (dichVu.getTenDV() == null || dichVu.getTenDV().isBlank()) {
            throw new SQLException("Tên dịch vụ không được để trống.");
        }
        if (dichVu.getGiaDV() != null && dichVu.getGiaDV() < 0) {
            throw new SQLException("Giá dịch vụ không được âm.");
        }
    }

    private void kiemTraMa(String maDV) throws SQLException {
        if (maDV == null || maDV.isBlank()) {
            throw new SQLException("Mã dịch vụ không được để trống.");
        }
    }
}
