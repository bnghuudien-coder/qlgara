package dao;

import entity.PhieuKiemTra;
import entity.PhieuTiepNhan;
import entity.KhachHang;
import entity.NhanVien;
import entity.Xe;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhieuKiemTraDAO {
    public List<PhieuKiemTra> findAll() throws SQLException {
        String sql = "SELECT maPhieuKiemTra, maPhieuTiepNhan, maKH, maXe, maNV, "
                + "trangThaiBanDau, vanDeHuHong FROM PhieuKiemTra ORDER BY maPhieuKiemTra";
        List<PhieuKiemTra> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(mapRow(resultSet));
            }
        }
        return danhSach;
    }

    public boolean insert(PhieuKiemTra phieu) throws SQLException {
        String sql = "INSERT INTO PhieuKiemTra "
                + "(maPhieuKiemTra, maPhieuTiepNhan, maKH, maXe, maNV, trangThaiBanDau, vanDeHuHong) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ganThamSo(statement, phieu);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(PhieuKiemTra phieu) throws SQLException {
        String sql = "UPDATE PhieuKiemTra SET maPhieuTiepNhan = ?, maKH = ?, maXe = ?, maNV = ?, "
                + "trangThaiBanDau = ?, vanDeHuHong = ? WHERE maPhieuKiemTra = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phieu.getPhieuTiepNhan() == null
                    ? null : phieu.getPhieuTiepNhan().getMaPhieuTiepNhan());
            statement.setString(2, phieu.getKhachHang() == null ? null : phieu.getKhachHang().getMaKH());
            statement.setString(3, phieu.getXe() == null ? null : phieu.getXe().getMaXe());
            statement.setString(4, phieu.getNhanVien() == null ? null : phieu.getNhanVien().getMaNV());
            statement.setString(5, phieu.getTrangThaiBanDau());
            statement.setString(6, phieu.getVanDeHuHong());
            statement.setString(7, phieu.getMaPhieuKiemTra());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String maPhieuKiemTra) throws SQLException {
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM PhieuKiemTra WHERE maPhieuKiemTra = ?")) {
            statement.setString(1, maPhieuKiemTra);
            return statement.executeUpdate() > 0;
        }
    }

    private PhieuKiemTra mapRow(ResultSet resultSet) throws SQLException {
        return new PhieuKiemTra(
                resultSet.getString("maPhieuKiemTra"),
                new PhieuTiepNhan(resultSet.getString("maPhieuTiepNhan")),
                new KhachHang(resultSet.getString("maKH")),
                new Xe(resultSet.getString("maXe")),
                new NhanVien(resultSet.getString("maNV")),
                resultSet.getString("trangThaiBanDau"),
                resultSet.getString("vanDeHuHong"));
    }

    private void ganThamSo(PreparedStatement statement, PhieuKiemTra phieu) throws SQLException {
        statement.setString(1, phieu.getMaPhieuKiemTra());
        statement.setString(2, phieu.getPhieuTiepNhan() == null
                ? null : phieu.getPhieuTiepNhan().getMaPhieuTiepNhan());
        statement.setString(3, phieu.getKhachHang() == null ? null : phieu.getKhachHang().getMaKH());
        statement.setString(4, phieu.getXe() == null ? null : phieu.getXe().getMaXe());
        statement.setString(5, phieu.getNhanVien() == null ? null : phieu.getNhanVien().getMaNV());
        statement.setString(6, phieu.getTrangThaiBanDau());
        statement.setString(7, phieu.getVanDeHuHong());
    }

    private Connection moKetNoi() throws SQLException {
        java.util.Properties properties = new java.util.Properties();
        try (var reader = Files.newBufferedReader(Path.of("database.properties"))) {
            properties.load(reader);
        } catch (IOException exception) {
            throw new SQLException("Không đọc được cấu hình database.properties.", exception);
        }
        return DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("user"),
                properties.getProperty("password"));
    }
}
